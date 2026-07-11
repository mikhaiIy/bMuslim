/* shared.js — bMuslim shared utilities (global, no module syntax)
 * Exposes:
 *   window.bmTheme   — theme manager
 *   window.bmIcons   — SVG icon library
 *   window.bmTime    — time helpers
 *   window.bmRing    — progress ring helper
 *   window.bmReveal  — scroll-reveal helper
 */
(function () {
  'use strict';

  /* ═══════════════════════════════════════════════════════════════════════════
   * Theme manager
   * ═══════════════════════════════════════════════════════════════════════════ */
  var THEME_KEY = 'bm-theme';

  function getSystemTheme() {
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
      ? 'dark'
      : 'light';
  }

  function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
  }

  function initTheme() {
    var stored = null;
    try { stored = localStorage.getItem(THEME_KEY); } catch (e) {}
    var theme = stored || getSystemTheme();
    applyTheme(theme);
    return theme;
  }

  function toggleTheme() {
    var current = document.documentElement.getAttribute('data-theme') || getSystemTheme();
    var next = current === 'dark' ? 'light' : 'dark';
    applyTheme(next);
    try { localStorage.setItem(THEME_KEY, next); } catch (e) {}
    /* Dispatch event so pages can react (e.g. update toggle button icon) */
    window.dispatchEvent(new CustomEvent('bm-theme-change', { detail: { theme: next } }));
    return next;
  }

  function currentTheme() {
    return document.documentElement.getAttribute('data-theme') || getSystemTheme();
  }

  window.bmTheme = {
    init:    initTheme,
    toggle:  toggleTheme,
    current: currentTheme
  };

  /* Listen for OS-level preference changes when no explicit preference stored */
  if (window.matchMedia) {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function (e) {
      var stored = null;
      try { stored = localStorage.getItem(THEME_KEY); } catch (err) {}
      if (!stored) {
        applyTheme(e.matches ? 'dark' : 'light');
      }
    });
  }

  /* ═══════════════════════════════════════════════════════════════════════════
   * Icon library
   * SVG strings, 1.8px stroke, currentColor, viewBox 0 0 24 24.
   * Reuses the exact icon set from design/home-mockup.html, plus sadaqah (hand).
   * ═══════════════════════════════════════════════════════════════════════════ */
  function ico(path, extra) {
    var attrs = 'viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"';
    if (extra) { attrs += ' ' + extra; }
    return '<svg xmlns="http://www.w3.org/2000/svg" ' + attrs + '>' + path + '</svg>';
  }

  window.bmIcons = {
    /* Navigation */
    home: ico('<path d="M4 11l8-7 8 7"/><path d="M6 10v9h12v-9"/>'),
    cal:  ico('<rect x="4" y="5" width="16" height="16" rx="3"/><path d="M4 9h16M9 3v4M15 3v4"/>'),
    gear: ico('<circle cx="12" cy="12" r="3.2"/><path d="M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M18.4 5.6l-2.1 2.1M7.7 16.3l-2.1 2.1"/>'),

    /* Actions */
    back:  ico('<path d="M15 5l-7 7 7 7"/>', 'width="20" height="20"'),
    plus:  ico('<path d="M12 5v14M5 12h14"/>', 'width="16" height="16"'),
    chev:  ico('<path d="M9 6l6 6-6 6"/>', 'width="18" height="18"'),
    check: ico('<path d="M5 12l4 4 10-10"/>'),
    close: ico('<path d="M18 6L6 18M6 6l12 12"/>'),

    /* Content types */
    salat: ico('<path d="M12 3v6"/><path d="M6 21v-6a6 6 0 0 1 12 0v6"/>'),
    zikir: ico('<circle cx="12" cy="12" r="8"/><circle cx="12" cy="12" r="2.4"/>'),
    quran: ico('<path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v15H6.5A2.5 2.5 0 0 0 4 20.5Z"/><path d="M12 6v9"/>'),
    hand:  ico('<path d="M18 11V6a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h4"/><path d="M12 13v6M9 16h6"/>'),

    /* Theme / time */
    moon: ico('<path d="M20 14.5A8 8 0 1 1 9.5 4a6.2 6.2 0 0 0 10.5 10.5Z"/>', 'width="19" height="19"'),
    sun:  ico('<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>'),

    /* Citation */
    link: ico('<path d="M10 13a5 5 0 0 0 7 0l2-2a5 5 0 0 0-7-7l-1 1"/><path d="M14 11a5 5 0 0 0-7 0l-2 2a5 5 0 0 0 7 7l1-1"/>'),
    doc:  ico('<path d="M6 3h8l4 4v14H6z"/><path d="M14 3v4h4M9 13h6M9 17h6"/>'),

    /* Misc */
    sadaqah: ico('<path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>')
  };

  /* Helper: return icon by deed type string */
  window.bmIcons.forType = function (type) {
    if (!type) { return window.bmIcons.zikir; }
    var t = type.toLowerCase();
    if (t === 'salah')   { return window.bmIcons.salat; }
    if (t === 'dhikr')   { return window.bmIcons.zikir; }
    if (t === 'quran')   { return window.bmIcons.quran; }
    if (t === 'sadaqah') { return window.bmIcons.sadaqah; }
    if (t === 'sawm')    { return window.bmIcons.moon; }
    return window.bmIcons.zikir;
  };

  /* ═══════════════════════════════════════════════════════════════════════════
   * Time helpers
   * ═══════════════════════════════════════════════════════════════════════════ */

  /**
   * fmt(decimalHours) → "HH:MM"
   * e.g. 13.35 → "13:21"
   */
  function fmt(h) {
    var hours   = Math.floor(h);
    var minutes = Math.round((h - hours) * 60);
    if (minutes === 60) { hours += 1; minutes = 0; }
    return String(hours).padStart(2, '0') + ':' + String(minutes).padStart(2, '0');
  }

  /**
   * clamp(value, min, max) → number
   */
  function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max);
  }

  /**
   * lerp(a, b, t) → number — linear interpolation
   */
  function lerp(a, b, t) {
    return a + (b - a) * clamp(t, 0, 1);
  }

  /**
   * parseTime(str) → decimal hours
   * e.g. "13:21" → 13.35
   */
  function parseTime(str) {
    var parts = str.split(':');
    return parseInt(parts[0], 10) + parseInt(parts[1], 10) / 60;
  }

  /**
   * hourToX factory — returns a function mapping decimal hours to pixel offset.
   * @param {number} pxPerHour  pixels per hour
   * @param {number} dayStart   decimal hours of left edge (e.g. 3.0)
   * @returns {function(number): number}
   */
  function makeHourToX(pxPerHour, dayStart) {
    return function (h) {
      return (h - dayStart) * pxPerHour;
    };
  }

  /**
   * xToHour factory — inverse of hourToX.
   * @param {number} pxPerHour
   * @param {number} dayStart
   * @returns {function(number): number}
   */
  function makeXToHour(pxPerHour, dayStart) {
    return function (px) {
      return px / pxPerHour + dayStart;
    };
  }

  /**
   * durationPx(startH, endH, pxPerHour) → pixel width
   */
  function durationPx(startH, endH, pxPerHour) {
    return (endH - startH) * pxPerHour;
  }

  /**
   * nearestDeed(deeds, currentHour) → deed | null
   * Returns the deed whose act time is nearest to currentHour and whose
   * window contains currentHour. Falls back to deed with act nearest overall.
   */
  function nearestDeed(deeds, currentHour) {
    var inside = deeds.filter(function (d) {
      return currentHour >= d.windowStart && currentHour <= d.windowEnd;
    });
    var pool = inside.length ? inside : deeds;
    var best = null, bestDist = Infinity;
    pool.forEach(function (d) {
      var dist = Math.abs(d.act - currentHour);
      if (dist < bestDist) { bestDist = dist; best = d; }
    });
    return best;
  }

  /**
   * deedsAtNeedle(deeds, needleHour) → deed[]
   * All deeds whose Window spans the needle position.
   */
  function deedsAtNeedle(deeds, needleHour) {
    return deeds.filter(function (d) {
      return needleHour >= d.windowStart && needleHour <= d.windowEnd;
    });
  }

  window.bmTime = {
    fmt:          fmt,
    clamp:        clamp,
    lerp:         lerp,
    parseTime:    parseTime,
    makeHourToX:  makeHourToX,
    makeXToHour:  makeXToHour,
    durationPx:   durationPx,
    nearestDeed:  nearestDeed,
    deedsAtNeedle: deedsAtNeedle
  };

  /* ═══════════════════════════════════════════════════════════════════════════
   * Progress ring helper
   * Mounts or updates an SVG progress ring into a container element.
   * ═══════════════════════════════════════════════════════════════════════════ */

  /**
   * mountProgressRing(container, options)
   *   container — DOM element with class .progress-ring
   *   options = { size, strokeWidth, value, total, label }
   *
   * Renders the ring SVG + label. Call again with updated value/total to animate.
   * Returns an update function: update(value, total).
   */
  function mountProgressRing(container, options) {
    var size   = options.size        || 52;
    var sw     = options.strokeWidth || 5;
    var value  = options.value       || 0;
    var total  = options.total       || 1;
    var label  = options.label       || '';
    var cx = size / 2;
    var cy = size / 2;
    var r  = cx - sw / 2 - 1;
    var circumference = 2 * Math.PI * r;

    function offset(v, t) {
      var ratio = t > 0 ? clamp(v / t, 0, 1) : 0;
      return circumference * (1 - ratio);
    }

    container.innerHTML =
      '<svg width="' + size + '" height="' + size + '" role="img" aria-label="Kemajuan ' + value + ' daripada ' + total + '">' +
        '<circle class="progress-ring-track" cx="' + cx + '" cy="' + cy + '" r="' + r + '" stroke-width="' + sw + '"/>' +
        '<circle class="progress-ring-fill" cx="' + cx + '" cy="' + cy + '" r="' + r + '" stroke-width="' + sw + '"' +
          ' stroke-dasharray="' + circumference + '"' +
          ' stroke-dashoffset="' + offset(value, total) + '"' +
          ' transform="rotate(-90 ' + cx + ' ' + cy + ')"/>' +
      '</svg>' +
      '<div class="progress-ring-label">' +
        '<b>' + value + '/' + total + '</b>' +
        '<span>' + (label || 'hari ini') + '</span>' +
      '</div>';

    var fillEl  = container.querySelector('.progress-ring-fill');
    var labelEl = container.querySelector('.progress-ring-label b');
    var ariaEl  = container.querySelector('svg');

    return function update(v, t) {
      v = v || 0; t = t || 1;
      if (fillEl)  { fillEl.setAttribute('stroke-dashoffset', String(offset(v, t))); }
      if (labelEl) { labelEl.textContent = v + '/' + t; }
      if (ariaEl)  { ariaEl.setAttribute('aria-label', 'Kemajuan ' + v + ' daripada ' + t); }
    };
  }

  window.bmRing = {
    mount: mountProgressRing
  };

  /* ═══════════════════════════════════════════════════════════════════════════
   * Scroll-reveal helper
   * Observes .reveal elements; adds .in-view when they enter the viewport.
   * ═══════════════════════════════════════════════════════════════════════════ */

  function initReveal() {
    /* Skip if user prefers reduced motion */
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      document.querySelectorAll('.reveal').forEach(function (el) {
        el.classList.add('in-view');
      });
      return;
    }

    if (!('IntersectionObserver' in window)) {
      /* Fallback: show all immediately */
      document.querySelectorAll('.reveal').forEach(function (el) {
        el.classList.add('in-view');
      });
      return;
    }

    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('in-view');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12 });

    document.querySelectorAll('.reveal').forEach(function (el) {
      io.observe(el);
    });
  }

  window.bmReveal = {
    init: initReveal
  };

  /* ═══════════════════════════════════════════════════════════════════════════
   * Auto-init on DOMContentLoaded
   * Theme must be applied immediately (before paint) to avoid flash.
   * ═══════════════════════════════════════════════════════════════════════════ */
  /* Apply theme immediately (script runs synchronously, before body renders) */
  window.bmTheme.init();

  document.addEventListener('DOMContentLoaded', function () {
    window.bmReveal.init();
  });

}());
