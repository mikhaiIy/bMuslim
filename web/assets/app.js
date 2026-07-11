/* app.js — bMuslim web app engine
 * Depends on: tokens.css, data.js (window.BM), shared.js (window.bmTheme, bmIcons, bmTime, bmRing)
 * Exposes: nothing public (self-contained app shell)
 *
 * Architecture:
 *   - Hash routing: #hari-ini | #perancang | #tetapan  (default: #hari-ini)
 *   - Timeline: horizontal scroll container, needle fixed at center, drag/wheel/keyboard scrub
 *   - Detail panel: reacts to needle position, deed selector when multiple overlap
 *   - Check-off state in localStorage (key: bm-done-<date>)
 *   - Settings in localStorage (key: bm-settings)
 *   - Progress ring updates live from check-off state
 */
(function () {
  'use strict';

  /* ─── Constants ────────────────────────────────────────────────────────────── */
  var BM        = window.BM;
  var bmTime    = window.bmTime;
  var bmIcons   = window.bmIcons;
  var bmRing    = window.bmRing;

  var PPH       = BM.config.pxPerHour;   /* 98 px per hour */
  var DAY_START = BM.config.dayStart;    /* 3.0 */
  var DAY_END   = BM.config.dayEnd;      /* 23.5 */
  var NOW_H     = BM.config.now;         /* 15.0 */
  var S         = BM.strings;

  var hourToXRaw = bmTime.makeHourToX(PPH, DAY_START);
  var xToHourRaw = bmTime.makeXToHour(PPH, DAY_START);
  var TRACK_W   = Math.round((DAY_END - DAY_START) * PPH);   /* content px width */

  /* Half-viewport spacer so the centre needle can reach 03:00 and 23:30.
     Computed from the scroll viewport in buildTimeline(); all hour<->px
     mapping goes through these wrappers so the offset stays consistent. */
  var PAD = 0;
  function hourToX(h) { return hourToXRaw(h) + PAD; }
  function xToHour(x) { return xToHourRaw(x - PAD); }

  var REDUCED = !!(window.matchMedia &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches);

  /* Storage keys */
  var DONE_KEY  = 'bm-done-' + BM.config.dateGreg.replace(/\s/g, '');
  var SETT_KEY  = 'bm-settings';
  var ADDED_KEY = 'bm-planner-added';

  /* ─── State ─────────────────────────────────────────────────────────────────── */
  var state = {
    needleHour:   NOW_H,
    selectedDeedId: null,
    done:         {},     /* id → true */
    settings: {
      zone:     BM.config.zone,
      sleep:    '23:00',
      wake:     '05:30',
      level:    'pemula',
      lang:     'bm',
      notifPrayer:     true,
      notifZikirPagi:  true,
      notifZikirPetang:true,
      notifAlMulk:     true,
      notifSummary:    false
    }
  };

  /* Progress ring updater (assigned after mount) */
  var updateRing = null;

  /* ─── Persistence helpers ────────────────────────────────────────────────── */
  function loadDone() {
    try {
      var raw = localStorage.getItem(DONE_KEY);
      return raw ? JSON.parse(raw) : {};
    } catch (e) { return {}; }
  }

  function saveDone() {
    try { localStorage.setItem(DONE_KEY, JSON.stringify(state.done)); } catch (e) {}
  }

  function loadSettings() {
    try {
      /* Migrate the legacy onboarding schema (bmuslim.settings with
         zon/tidur/bangun/tahap/bahasa) into the app schema, once. */
      var legacyRaw = localStorage.getItem('bmuslim.settings');
      if (legacyRaw && !localStorage.getItem(SETT_KEY)) {
        var legacy = JSON.parse(legacyRaw);
        var migrated = {};
        if (legacy.zon)    migrated.zone  = legacy.zon;
        if (legacy.tidur)  migrated.sleep = legacy.tidur;
        if (legacy.bangun) migrated.wake  = legacy.bangun;
        if (legacy.tahap)  migrated.level = legacy.tahap;
        if (legacy.bahasa) migrated.lang  = legacy.bahasa;
        localStorage.setItem(SETT_KEY, JSON.stringify(Object.assign({}, state.settings, migrated)));
      }
      var raw = localStorage.getItem(SETT_KEY);
      if (raw) {
        var parsed = JSON.parse(raw);
        Object.assign(state.settings, parsed);
      }
    } catch (e) {}
  }

  function saveSettings() {
    try { localStorage.setItem(SETT_KEY, JSON.stringify(state.settings)); } catch (e) {}
  }

  /* ─── Deed helpers ───────────────────────────────────────────────────────── */
  function deedTypeLabel(type) {
    var map = { Salah:'Solat', Dhikr:'Zikir', Quran:'Quran', Sadaqah:'Sedekah', Sawm:'Puasa', Adab:'Adab' };
    return map[type] || type;
  }

  function rulingLabel(ruling) {
    var map = { fardh:'Fardhu', muakkadah:'Sunnah Muakkadah', sunnah:'Sunnah', mandub:'Mandub' };
    return map[ruling] || ruling;
  }

  function countDone() {
    /* Count deeds (excluding fardh for now — progress is for sunnah deeds) */
    /* Actually count all checkable deeds */
    return Object.keys(state.done).filter(function (k) { return state.done[k]; }).length;
  }

  function totalDeeds() {
    /* All deeds + anytime */
    return BM.deeds.length + BM.anytime.length;
  }

  /* ─── Lane assignment (overlap stacking) ─────────────────────────────────── */
  /* Returns deeds sorted into lanes. Lane 0 = prayer base lane, Lane 1+ = deed lanes. */
  /* Prayers get their own base lane. Non-fardh deeds stack into additional lanes. */
  function assignLanes(deeds) {
    var result = [];
    var lanes  = [];  /* array of "last end time" per lane */

    deeds.forEach(function (d) {
      var placed = false;
      for (var i = 0; i < lanes.length; i++) {
        if (d.windowStart >= lanes[i]) {
          lanes[i]   = d.windowEnd;
          result.push({ deed: d, lane: i });
          placed = true;
          break;
        }
      }
      if (!placed) {
        lanes.push(d.windowEnd);
        result.push({ deed: d, lane: lanes.length - 1 });
      }
    });
    return result;
  }

  /* ─── DOM references ─────────────────────────────────────────────────────── */
  var els = {};

  function qsel(selector) {
    return document.querySelector(selector);
  }

  function refs() {
    els.topbar          = qsel('#topbar');
    els.navBtns         = document.querySelectorAll('.nav-pill');
    els.views           = {
      hariIni:   qsel('#view-hari-ini'),
      perancang: qsel('#view-perancang'),
      tetapan:   qsel('#view-tetapan')
    };
    els.tlScroll        = qsel('#tl-scroll');
    els.tlTrack         = qsel('#tl-track');
    els.needleChip      = qsel('#needle-chip');
    els.backToNowBtn    = qsel('#back-to-now');
    els.detailNowLabel  = qsel('#detail-now-label');
    els.detailSelector  = qsel('#detail-selector');
    els.detailContent   = qsel('#detail-content');
    els.ringContainer   = qsel('#progress-ring');
    els.themeToggle     = qsel('#theme-toggle');
  }

  /* ─── View routing ───────────────────────────────────────────────────────── */
  function getView() {
    var hash = window.location.hash || '#hari-ini';
    if (hash === '#perancang') return 'perancang';
    if (hash === '#tetapan')   return 'tetapan';
    return 'hari-ini';
  }

  function showView(viewName) {
    Object.keys(els.views).forEach(function (k) {
      var active = (k === camelHash(viewName));
      els.views[k].hidden = !active;
    });
    els.navBtns.forEach(function (btn) {
      var isActive = btn.dataset.view === viewName;
      btn.classList.toggle('active', isActive);
      btn.setAttribute('aria-current', isActive ? 'page' : 'false');
    });
  }

  function camelHash(viewName) {
    if (viewName === 'hari-ini')  return 'hariIni';
    if (viewName === 'perancang') return 'perancang';
    if (viewName === 'tetapan')   return 'tetapan';
    return 'hariIni';
  }

  function navigate(viewName) {
    window.location.hash = '#' + viewName;
    showView(viewName);
  }

  /* ─── Timeline rendering ─────────────────────────────────────────────────── */
  function buildTimeline() {
    var track = els.tlTrack;
    PAD = els.tlScroll ? Math.round(els.tlScroll.clientWidth / 2) : 0;
    track.style.width = (TRACK_W + 2 * PAD) + 'px';
    track.innerHTML = '';

    /* Hour ticks + labels */
    var ticksEl = document.createElement('div');
    ticksEl.className = 'tl-ticks';
    for (var h = Math.ceil(DAY_START); h <= DAY_END; h++) {
      var isMajor = (h % 2 === 0);
      var tick = document.createElement('div');
      tick.className = 'tl-tick' + (isMajor ? ' tl-tick-major' : '');
      tick.style.left = hourToX(h) + 'px';
      ticksEl.appendChild(tick);

      if (isMajor) {
        var lbl = document.createElement('div');
        lbl.className = 'tl-tick-label';
        lbl.style.left = hourToX(h) + 'px';
        lbl.textContent = String(h).padStart(2, '0');
        ticksEl.appendChild(lbl);
      }
    }
    track.appendChild(ticksEl);

    /* Prayer base lane */
    var prayerLane = document.createElement('div');
    prayerLane.className = 'tl-lane tl-lane-prayer';
    prayerLane.setAttribute('aria-label', 'Solat fardhu');

    var laneLabelP = document.createElement('span');
    laneLabelP.className = 'tl-lane-label';
    laneLabelP.textContent = 'Solat fardhu';
    prayerLane.appendChild(laneLabelP);

    /* Fardhu windows: each prayer may be performed until the next one, so the
       base lane shows that range as a faint track behind the time dots. */
    BM.deeds.filter(function (d) { return d.ruling === 'fardh'; }).forEach(function (d) {
      var winP = document.createElement('div');
      winP.className = 'tl-window tl-window-prayer';
      winP.style.left  = hourToX(d.windowStart) + 'px';
      winP.style.width = bmTime.durationPx(d.windowStart, d.windowEnd, PPH) + 'px';
      prayerLane.appendChild(winP);
    });

    BM.prayers.forEach(function (prayer) {
      if (prayer.isSyuruk) {
        /* Syuruk: show as a small axis tick, not a full chip */
        var syuruk = document.createElement('div');
        syuruk.className = 'tl-syuruk';
        syuruk.style.left = hourToX(prayer.time) + 'px';
        var syurukLabel = document.createElement('div');
        syurukLabel.className = 'tl-syuruk-label';
        syurukLabel.textContent = 'Syuruk';
        syuruk.appendChild(syurukLabel);
        prayerLane.appendChild(syuruk);
        return;
      }

      var pEl = document.createElement('div');
      pEl.className = 'tl-prayer';
      pEl.style.left = hourToX(prayer.time) + 'px';
      pEl.innerHTML =
        '<div class="tl-prayer-dot">' + bmTime.fmt(prayer.time) + '</div>' +
        '<div class="tl-prayer-name">' + prayer.name + '</div>';
      prayerLane.appendChild(pEl);
    });

    track.appendChild(prayerLane);

    /* Deed lanes — filter out fardh prayers (shown in prayer lane) */
    var deedPool = BM.deeds.filter(function (d) { return d.ruling !== 'fardh'; });
    var assigned = assignLanes(deedPool);

    /* Find max lane index */
    var maxLane = 0;
    assigned.forEach(function (a) { if (a.lane > maxLane) maxLane = a.lane; });

    /* One DOM lane element per lane */
    for (var li = 0; li <= maxLane; li++) {
      var laneEl = document.createElement('div');
      laneEl.className = 'tl-lane tl-lane-deed';
      laneEl.setAttribute('aria-label', 'Amalan sunnah');

      if (li === 0) {
        var laneLabelD = document.createElement('span');
        laneLabelD.className = 'tl-lane-label tl-lane-label-deed';
        laneLabelD.textContent = 'Amalan sunnah';
        laneEl.appendChild(laneLabelD);
      }

      assigned.filter(function (a) { return a.lane === li; }).forEach(function (a) {
        var d = a.deed;

        /* Window track (faint dashed) */
        var winEl = document.createElement('div');
        winEl.className = 'tl-window';
        winEl.style.left  = hourToX(d.windowStart) + 'px';
        winEl.style.width = bmTime.durationPx(d.windowStart, d.windowEnd, PPH) + 'px';
        laneEl.appendChild(winEl);

        /* Act marker (solid, ruling = visual weight) */
        var actEl = document.createElement('div');
        actEl.className = 'tl-act tl-act-' + d.ruling;
        actEl.style.left = hourToX(d.act) + 'px';
        actEl.dataset.deedId = d.id;
        actEl.setAttribute('tabindex', '0');
        actEl.setAttribute('role', 'button');
        actEl.setAttribute('aria-label', d.title);
        actEl.innerHTML =
          '<span class="tl-act-icon" aria-hidden="true">' + bmIcons.forType(d.type) + '</span>' +
          '<span class="tl-act-title">' + d.title + '</span>';

        actEl.addEventListener('click', function () {
          var id = this.dataset.deedId;
          state.selectedDeedId = id;
          /* Bring the needle to this deed so the selection is never rejected
             for being outside the deed's window. */
          var deed = null;
          for (var i = 0; i < BM.deeds.length; i++) {
            if (BM.deeds[i].id === id) { deed = BM.deeds[i]; break; }
          }
          if (deed) { scrollToHour(deed.act, !REDUCED); }
          renderDetail();
        });
        actEl.addEventListener('keydown', function (e) {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            this.click();
          }
        });

        laneEl.appendChild(actEl);
      });

      track.appendChild(laneEl);
    }
  }

  /* ─── Needle position + scrubbing ────────────────────────────────────────── */
  var _scrubTarget    = null;   /* requested scroll position */
  var _rafPending     = false;
  var _momentum       = 0;
  var _lastPointerX   = null;
  var _isDragging     = false;
  var _lastDragTime   = 0;
  var _lastDragDelta  = 0;

  function getNeedleScrollLeft() {
    /* Scroll position so that needleHour is centered */
    var el = els.tlScroll;
    var center = el ? el.clientWidth / 2 : 0;
    return hourToX(state.needleHour) - center;
  }

  function scrollToHour(h, animated) {
    if (!els.tlScroll) return;
    var target = hourToX(h) - els.tlScroll.clientWidth / 2;
    target = Math.max(0, Math.min(target, TRACK_W + 2 * PAD - els.tlScroll.clientWidth));
    if (animated && !REDUCED) {
      els.tlScroll.style.scrollBehavior = 'smooth';
      els.tlScroll.scrollLeft = target;
      els.tlScroll.style.scrollBehavior = '';
    } else {
      els.tlScroll.scrollLeft = target;
    }
  }

  function syncNeedleFromScroll() {
    if (!els.tlScroll) return;
    var center = els.tlScroll.clientWidth / 2;
    var h = xToHour(els.tlScroll.scrollLeft + center);
    h = bmTime.clamp(h, DAY_START, DAY_END);
    state.needleHour = h;
    updateNeedleChip();
    updateBackToNow();
    updateDetail();
    announceNeedle();
  }

  /* Screen-reader announcement: only after the scrub settles, so the
     announcement queue is not flooded during movement. */
  var _liveRegion = null;
  var _announceTimer = null;
  function announceNeedle() {
    if (!_liveRegion) return;
    clearTimeout(_announceTimer);
    _announceTimer = setTimeout(function () {
      var current = deedsAtNow();
      var what = current.length ? current.map(function (d) { return d.title; }).join(', ') : S.noBlocksNeedle;
      _liveRegion.textContent = bmTime.fmt(state.needleHour) + '. ' + what;
    }, 700);
  }

  function updateNeedleChip() {
    if (els.needleChip) {
      els.needleChip.textContent = bmTime.fmt(state.needleHour);
    }
  }

  function updateBackToNow() {
    if (!els.backToNowBtn) return;
    var diff = Math.abs(state.needleHour - NOW_H);
    els.backToNowBtn.hidden = (diff < 0.08);  /* ~5 min threshold */
  }

  /* Throttle detail updates for performance */
  var _detailUpdateTimer = null;
  function updateDetail() {
    if (_detailUpdateTimer) return;
    _detailUpdateTimer = setTimeout(function () {
      _detailUpdateTimer = null;
      renderDetail();
    }, 60);
  }

  /* Pointer drag */
  function initScrubbing() {
    var sc = els.tlScroll;
    if (!sc) return;

    /* Mouse drag */
    sc.addEventListener('mousedown', function (e) {
      if (e.button !== 0) return;
      _isDragging    = true;
      _lastPointerX  = e.clientX;
      _momentum      = 0;
      _lastDragTime  = Date.now();
      _lastDragDelta = 0;
      sc.style.cursor = 'grabbing';
      sc.style.userSelect = 'none';
      e.preventDefault();
    });

    window.addEventListener('mousemove', function (e) {
      if (!_isDragging) return;
      var dx   = _lastPointerX - e.clientX;
      var now  = Date.now();
      var dt   = now - _lastDragTime || 1;
      _lastDragDelta = dx;
      _lastDragTime  = now;
      _lastPointerX  = e.clientX;
      sc.scrollLeft += dx;
      _momentum = dx / dt * 16;  /* pixels per frame */
      syncNeedleFromScroll();
    });

    window.addEventListener('mouseup', function () {
      if (!_isDragging) return;
      _isDragging = false;
      sc.style.cursor = '';
      sc.style.userSelect = '';
      startMomentum(sc);
    });

    /* Touch: native horizontal scrolling only (touch-action: pan-x in CSS).
       The scroll listener below keeps the needle in sync. */

    /* Wheel / trackpad */
    sc.addEventListener('wheel', function (e) {
      e.preventDefault();
      sc.scrollLeft += e.deltaX || e.deltaY;
      _momentum = 0;
      syncNeedleFromScroll();
    }, { passive: false });

    /* Keyboard arrows (when container focused) */
    sc.setAttribute('tabindex', '0');
    sc.addEventListener('keydown', function (e) {
      var step = 0;
      if (e.key === 'ArrowRight') { step = PPH / 4; }   /* 15 min */
      if (e.key === 'ArrowLeft')  { step = -PPH / 4; }
      if (e.key === 'ArrowUp')    { step = -PPH; }       /* 1 hour */
      if (e.key === 'ArrowDown')  { step = PPH; }
      if (!step) return;
      e.preventDefault();
      sc.scrollLeft += step;
      syncNeedleFromScroll();
    });

    /* Sync on scroll (covers programmatic and native) */
    sc.addEventListener('scroll', function () {
      syncNeedleFromScroll();
    }, { passive: true });
  }

  function startMomentum(sc) {
    if (Math.abs(_momentum) < 0.5) return;
    function step() {
      if (Math.abs(_momentum) < 0.5 || _isDragging) return;
      sc.scrollLeft += _momentum;
      _momentum *= 0.88;
      syncNeedleFromScroll();
      requestAnimationFrame(step);
    }
    requestAnimationFrame(step);
  }

  /* ─── Detail panel ────────────────────────────────────────────────────────── */
  function deedsAtNow() {
    return bmTime.deedsAtNeedle(BM.deeds, state.needleHour);
  }

  function renderDetail() {
    if (!els.detailContent) return;

    var nowStr = bmTime.fmt(state.needleHour);
    if (els.detailNowLabel) {
      els.detailNowLabel.textContent = S.detailNow + ' ' + nowStr;
    }

    var current = deedsAtNow();

    /* Build selector row if multiple deeds */
    if (els.detailSelector) {
      if (current.length > 1) {
        /* Ensure selectedDeedId is valid */
        var ids = current.map(function (d) { return d.id; });
        if (!state.selectedDeedId || ids.indexOf(state.selectedDeedId) === -1) {
          state.selectedDeedId = ids[0];
        }
        els.detailSelector.hidden = false;
        var html = '<span class="detail-selector-label">' + S.selectorLabel + '</span><div class="detail-selector-pills">';
        current.forEach(function (d) {
          var active = d.id === state.selectedDeedId;
          html += '<button class="detail-pill' + (active ? ' active' : '') + '" data-deed-id="' + d.id + '">' + d.title + '</button>';
        });
        html += '</div>';
        els.detailSelector.innerHTML = html;
        els.detailSelector.querySelectorAll('.detail-pill').forEach(function (btn) {
          btn.addEventListener('click', function () {
            state.selectedDeedId = this.dataset.deedId;
            renderDetail();
          });
        });
      } else {
        els.detailSelector.hidden = true;
        state.selectedDeedId = current.length ? current[0].id : null;
      }
    }

    /* Find deed to display */
    var deed = null;
    if (current.length) {
      if (state.selectedDeedId) {
        deed = current.find(function (d) { return d.id === state.selectedDeedId; }) || current[0];
      } else {
        deed = current[0];
      }
    }

    if (!deed) {
      /* No deed at needle — show empty state */
      els.detailContent.innerHTML =
        '<p class="detail-empty">' + S.noBlocksNeedle + '</p>';
      return;
    }

    var isDone = !!state.done[deed.id];
    var windowStr = bmTime.fmt(deed.windowStart) + ' - ' + bmTime.fmt(deed.windowEnd);

    var html = '<div class="detail-deed">';

    /* Title + checkbox row */
    html += '<label class="detail-title-row checkbox-wrap">';
    html +=   '<input type="checkbox" class="checkbox" id="deed-check-' + deed.id + '"' + (isDone ? ' checked' : '') + ' data-deed-id="' + deed.id + '">';
    html +=   '<span class="detail-deed-title">' + deed.title + '</span>';
    html += '</label>';

    /* Chips */
    html += '<div class="detail-chips">';
    html +=   '<span class="chip chip-type">' + deedTypeLabel(deed.type) + '</span>';
    html +=   '<span class="chip chip-ruling">' + rulingLabel(deed.ruling) + '</span>';
    html +=   '<span class="chip chip-time">' + windowStr + '</span>';
    html += '</div>';

    /* Arabic (if available) */
    if (deed.arabic) {
      html += '<div class="detail-arabic-wrap card card-padded">';
      html +=   '<div class="arabic arabic-lg">' + deed.arabic + '</div>';
      if (deed.transliteration) {
        html += '<div class="detail-translit">' + deed.transliteration + '</div>';
      }
      html += '</div>';
    }

    /* Translation / description */
    if (deed.translation) {
      html += '<div class="detail-section-label section-label">' + S.meaningLabel + '</div>';
      html += '<p class="detail-translation">' + deed.translation + '</p>';
    } else if (deed.description) {
      html += '<p class="detail-description">' + deed.description + '</p>';
    }

    /* Citation card */
    if (deed.citation) {
      html += '<div class="detail-section-label section-label">' + S.citationLabel + '</div>';
      html += '<div class="citation-card">';
      html +=   '<div class="citation-row">';
      html +=     '<span class="citation-icon">' + bmIcons.doc + '</span>';
      html +=     '<span class="citation-label">' + deed.citation.source + '</span>';
      html +=     '<span class="citation-grade chip chip-grade">' + deed.citation.grade + '</span>';
      html +=   '</div>';
      if (deed.citation.sunnahUrl) {
        html += '<a href="' + deed.citation.sunnahUrl + '" target="_blank" rel="noopener" class="citation-row">';
        html +=   '<span class="citation-icon">' + bmIcons.link + '</span>';
        html +=   '<span class="citation-label">' + S.viewSunnah + '</span>';
        html +=   '<span class="citation-chev">' + bmIcons.chev + '</span>';
        html += '</a>';
      }
      if (deed.citation.fatwaUrl) {
        html += '<a href="' + deed.citation.fatwaUrl + '" target="_blank" rel="noopener" class="citation-row">';
        html +=   '<span class="citation-icon">' + bmIcons.link + '</span>';
        html +=   '<span class="citation-label">' + S.viewFatwa + '</span>';
        html +=   '<span class="citation-chev">' + bmIcons.chev + '</span>';
        html += '</a>';
      }
      html += '</div>';
    }

    /* Mark done button */
    html += '<button class="btn btn-primary btn-full detail-mark-btn" data-deed-id="' + deed.id + '">';
    html +=   isDone ? S.markUndone : S.markDone;
    html += '</button>';

    html += '</div>'; /* .detail-deed */
    els.detailContent.innerHTML = html;

    /* Checkbox listener */
    var checkEl = els.detailContent.querySelector('.checkbox');
    if (checkEl) {
      checkEl.addEventListener('change', function () {
        var id = this.dataset.deedId;
        state.done[id] = this.checked;
        saveDone();
        refreshRing();
        renderDetail(); /* re-render to update button label */
      });
    }

    /* Mark done button */
    var markBtn = els.detailContent.querySelector('.detail-mark-btn');
    if (markBtn) {
      markBtn.addEventListener('click', function () {
        var id = this.dataset.deedId;
        state.done[id] = !state.done[id];
        saveDone();
        refreshRing();
        renderDetail();
      });
    }
  }

  /* ─── Progress ring ───────────────────────────────────────────────────────── */
  function refreshRing() {
    var done  = countDone();
    var total = totalDeeds();
    if (updateRing) { updateRing(done, total); }
  }

  /* ─── Anytime tray ────────────────────────────────────────────────────────── */
  function renderAnytime() {
    var container = qsel('#anytime-tray');
    if (!container) return;

    var html = '';
    BM.anytime.forEach(function (item) {
      var isDone = !!state.done[item.id];
      html += '<div class="anytime-card card">';
      html +=   '<label class="checkbox-wrap">';
      html +=     '<input type="checkbox" class="checkbox" data-deed-id="' + item.id + '"' + (isDone ? ' checked' : '') + '>';
      html +=     '<div class="anytime-body">';
      html +=       '<div class="anytime-icon-wrap">';
      html +=         '<span class="anytime-type-icon" aria-hidden="true">' + bmIcons.forType(item.type) + '</span>';
      html +=       '</div>';
      html +=       '<span class="anytime-title">' + item.title + '</span>';
      if (item.description) {
        html +=     '<p class="anytime-desc">' + item.description + '</p>';
      }
      html +=     '</div>';
      html +=   '</label>';
      html += '</div>';
    });

    container.innerHTML = html;

    container.querySelectorAll('.checkbox').forEach(function (cb) {
      cb.addEventListener('change', function () {
        state.done[this.dataset.deedId] = this.checked;
        saveDone();
        refreshRing();
      });
    });
  }

  /* ─── Perancang view ──────────────────────────────────────────────────────── */
  function renderPerancang() {
    var weekEl = qsel('#week-strip');
    if (!weekEl) return;

    var html = '';
    BM.planner.weekDays.forEach(function (day) {
      html += '<div class="week-cell' + (day.active ? ' week-cell-active' : '') + '" role="listitem">';
      html +=   '<div class="week-cell-name">' + day.short + '</div>';
      html +=   '<div class="week-cell-num">' + day.num + '</div>';
      if (day.fast) {
        html += '<div class="week-cell-fast" aria-label="Puasa sunat"></div>';
      }
      html += '</div>';
    });
    weekEl.innerHTML = html;

    var suggestEl = qsel('#suggestions-grid');
    if (!suggestEl) return;

    var added = {};
    try { added = JSON.parse(localStorage.getItem(ADDED_KEY)) || {}; } catch (err) { added = {}; }

    html = '';
    BM.planner.suggestions.forEach(function (s) {
      var isGold = s.type === 'Sawm';
      var isAdded = !!added[s.id];
      html += '<div class="suggestion-card card" role="listitem">';
      html +=   '<div class="icon-box' + (isGold ? ' icon-box-gold' : '') + '">';
      html +=     bmIcons.forType(s.type);
      html +=   '</div>';
      html +=   '<div class="suggestion-body">';
      html +=     '<div class="suggestion-title">' + s.title + '</div>';
      html +=     '<div class="suggestion-desc">' + s.desc + '</div>';
      html +=   '</div>';
      html +=   '<button class="suggestion-add btn btn-ghost' + (isAdded ? ' added' : '') + '"' +
                ' data-suggestion-id="' + s.id + '" aria-pressed="' + isAdded + '"' +
                ' aria-label="Tambah ' + s.title + '">' +
                (isAdded ? bmIcons.check : bmIcons.plus) + '</button>';
      html += '</div>';
    });
    suggestEl.innerHTML = html;

    suggestEl.querySelectorAll('.suggestion-add').forEach(function (btn) {
      btn.addEventListener('click', function () {
        var id = this.dataset.suggestionId;
        added[id] = !added[id];
        try { localStorage.setItem(ADDED_KEY, JSON.stringify(added)); } catch (err) { /* private mode */ }
        this.classList.toggle('added', added[id]);
        this.setAttribute('aria-pressed', added[id] ? 'true' : 'false');
        this.innerHTML = added[id] ? bmIcons.check : bmIcons.plus;
      });
    });
  }

  /* ─── Tetapan view ────────────────────────────────────────────────────────── */
  function renderTetapan() {
    /* Populate select / inputs with current settings */
    var zoneEl  = qsel('#setting-zone');
    var sleepEl = qsel('#setting-sleep');
    var wakeEl  = qsel('#setting-wake');
    if (zoneEl && !zoneEl.dataset.populated) {
      zoneEl.innerHTML = BM.zones.map(function (z) {
        return '<option value="' + z.code + '">' + z.code + ': ' + z.area + '</option>';
      }).join('');
      zoneEl.dataset.populated = '1';
    }
    if (zoneEl)  zoneEl.value  = state.settings.zone;
    if (sleepEl) sleepEl.value = state.settings.sleep;
    if (wakeEl)  wakeEl.value  = state.settings.wake;

    /* Level segmented */
    var levelBtns = document.querySelectorAll('.setting-level-btn');
    levelBtns.forEach(function (btn) {
      var on = btn.dataset.value === state.settings.level;
      btn.classList.toggle('active', on);
      btn.setAttribute('aria-pressed', on ? 'true' : 'false');
    });

    /* Lang segmented */
    var langBtns = document.querySelectorAll('.setting-lang-btn');
    langBtns.forEach(function (btn) {
      var on = btn.dataset.value === state.settings.lang;
      btn.classList.toggle('active', on);
      btn.setAttribute('aria-pressed', on ? 'true' : 'false');
    });

    /* Notification toggles */
    [
      ['notif-prayer',      'notifPrayer'],
      ['notif-zikir-pagi',  'notifZikirPagi'],
      ['notif-zikir-petang','notifZikirPetang'],
      ['notif-al-mulk',     'notifAlMulk'],
      ['notif-summary',     'notifSummary']
    ].forEach(function (pair) {
      var el = qsel('#' + pair[0]);
      if (el) { el.checked = !!state.settings[pair[1]]; }
    });
  }

  var _tetapanBound = false;
  function initTetapanEvents() {
    if (_tetapanBound) return;
    _tetapanBound = true;

    /* Zone */
    var zoneEl = qsel('#setting-zone');
    if (zoneEl) {
      zoneEl.addEventListener('change', function () {
        state.settings.zone = this.value;
        saveSettings();
      });
    }

    /* Sleep / wake */
    var sleepEl = qsel('#setting-sleep');
    if (sleepEl) {
      sleepEl.addEventListener('change', function () {
        state.settings.sleep = this.value;
        saveSettings();
      });
    }
    var wakeEl = qsel('#setting-wake');
    if (wakeEl) {
      wakeEl.addEventListener('change', function () {
        state.settings.wake = this.value;
        saveSettings();
      });
    }

    /* Level */
    document.querySelectorAll('.setting-level-btn').forEach(function (btn) {
      btn.addEventListener('click', function () {
        state.settings.level = this.dataset.value;
        saveSettings();
        document.querySelectorAll('.setting-level-btn').forEach(function (b) {
          var on = b.dataset.value === state.settings.level;
          b.classList.toggle('active', on);
          b.setAttribute('aria-pressed', on ? 'true' : 'false');
        });
      });
    });

    /* Lang */
    document.querySelectorAll('.setting-lang-btn').forEach(function (btn) {
      btn.addEventListener('click', function () {
        state.settings.lang = this.dataset.value;
        saveSettings();
        document.querySelectorAll('.setting-lang-btn').forEach(function (b) {
          var on = b.dataset.value === state.settings.lang;
          b.classList.toggle('active', on);
          b.setAttribute('aria-pressed', on ? 'true' : 'false');
        });
      });
    });

    /* Notification toggles */
    [
      ['notif-prayer',      'notifPrayer'],
      ['notif-zikir-pagi',  'notifZikirPagi'],
      ['notif-zikir-petang','notifZikirPetang'],
      ['notif-al-mulk',     'notifAlMulk'],
      ['notif-summary',     'notifSummary']
    ].forEach(function (pair) {
      var el = qsel('#' + pair[0]);
      if (el) {
        el.addEventListener('change', function () {
          state.settings[pair[1]] = this.checked;
          saveSettings();
        });
      }
    });
  }

  /* ─── Theme toggle ────────────────────────────────────────────────────────── */
  function updateThemeIcon() {
    var btn = els.themeToggle;
    if (!btn) return;
    var isDark = window.bmTheme.current() === 'dark';
    btn.innerHTML = isDark ? bmIcons.sun : bmIcons.moon;
    btn.setAttribute('aria-label', isDark ? 'Tukar ke mod cerah' : 'Tukar ke mod gelap');
  }

  /* ─── Initialise ──────────────────────────────────────────────────────────── */
  function init() {
    /* Load persisted state */
    state.done = loadDone();
    loadSettings();

    /* Grab DOM refs */
    refs();

    /* Theme toggle button */
    if (els.themeToggle) {
      updateThemeIcon();
      els.themeToggle.addEventListener('click', function () {
        window.bmTheme.toggle();
        updateThemeIcon();
      });
    }

    window.addEventListener('bm-theme-change', function () {
      updateThemeIcon();
    });

    /* Nav pill buttons */
    els.navBtns.forEach(function (btn) {
      btn.addEventListener('click', function () {
        navigate(this.dataset.view);
      });
    });

    /* Initial route */
    showView(getView());

    window.addEventListener('hashchange', function () {
      var view = getView();
      showView(view);
      if (view === 'hari-ini')  ensureTimelineGeometry();
      if (view === 'perancang') renderPerancang();
      if (view === 'tetapan')   { renderTetapan(); initTetapanEvents(); }
    });

    /* Progress ring */
    if (els.ringContainer) {
      var done  = countDone();
      var total = totalDeeds();
      updateRing = bmRing.mount(els.ringContainer, {
        size: 52, strokeWidth: 5, value: done, total: total, label: S.progressToday
      });
    }

    /* Build timeline */
    buildTimeline();

    /* Open centered on 15:00 (NOW_H) */
    /* Must happen after layout — use rAF to let the browser measure */
    requestAnimationFrame(function () {
      requestAnimationFrame(function () {
        scrollToHour(NOW_H, false);
        syncNeedleFromScroll();
        renderDetail();
      });
    });

    /* Scrubbing */
    initScrubbing();

    /* Back to now */
    if (els.backToNowBtn) {
      els.backToNowBtn.addEventListener('click', function () {
        scrollToHour(NOW_H, true);
        setTimeout(syncNeedleFromScroll, 350);
      });
    }

    /* Anytime tray */
    renderAnytime();

    /* Perancang / Tetapan may be rendered on first visit */
    renderPerancang();
    renderTetapan();
    initTetapanEvents();

    /* Screen-reader live region (see announceNeedle) */
    _liveRegion = document.createElement('div');
    _liveRegion.className = 'sr-only';
    _liveRegion.setAttribute('aria-live', 'polite');
    document.body.appendChild(_liveRegion);

    /* Keep needle geometry honest across resizes / orientation changes */
    var _resizeTimer = null;
    window.addEventListener('resize', function () {
      clearTimeout(_resizeTimer);
      _resizeTimer = setTimeout(ensureTimelineGeometry, 150);
    });
  }

  /* Rebuild the track when the viewport half-width no longer matches PAD
     (resize, orientation change, or first reveal of a view built while hidden). */
  function ensureTimelineGeometry() {
    if (!els.tlScroll) return;
    var want = Math.round(els.tlScroll.clientWidth / 2);
    if (want === 0 || want === PAD) return;
    var keep = state.needleHour;
    buildTimeline();
    scrollToHour(keep, false);
    syncNeedleFromScroll();
  }

  /* Wait for DOM */
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

}());
