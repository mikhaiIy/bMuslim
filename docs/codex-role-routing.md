# Codex role routing

The project's main Codex session defaults to `gpt-5.6-sol` with `xhigh`
reasoning through `.codex/config.toml`.

Run a role with an explicit model boundary:

```bash
scripts/codex-role research_scout "Research the assigned source question."
scripts/codex-role design_judge "Review the supplied design directions."
scripts/codex-role native_executor "Implement the accepted execution brief."
```

The launcher routes research, synthesis, orchestration, and judgment roles to
`gpt-5.6-sol` at `xhigh`. It routes only `native_executor` to
`gpt-5.6-luna` at `max`. Read-only roles receive a read-only sandbox; the
executor receives workspace-write.

Custom role definitions also live in `.codex/agents/`. Current custom-agent
configuration supports per-role model settings, but a client must expose the
registered agent type when it spawns the child. If its `spawn_agent` call only
accepts a task name, the child inherits the parent model. Use `codex-role` when
the model boundary must be guaranteed.

Verify account model availability with:

```bash
codex debug models | jq -r '.models[] | select(.slug == "gpt-5.6-sol" or .slug == "gpt-5.6-luna") | [.slug, ([.supported_reasoning_levels[].effort] | join(","))] | @tsv'
```
