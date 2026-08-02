<!--
Modrinth project description. Paste the content below (everything under the divider)
into the project's Description field.

Before publishing:
  - Replace the screenshot placeholders with your own captures. Do not reuse JackFred's
    images from the original project page.
  - Set the project name to "ChestTracker Reloaded", NOT "Chest Tracker" - Modrinth
    disallows listings that duplicate or impersonate an existing project.
  - Tick "client-side only" for environment.
  - Set licence to LGPL-3.0-only and the source link to the GitHub repo. The source
    link is a licence requirement, not a nicety.
-->

---

# Chest Tracker

**Never lose track of your items again.** Chest Tracker remembers what you put in every container,
then shows you exactly where it is when you search for it.

> ### ⚠️ This is an unofficial fork
>
> This is a community fork of **[Chest Tracker](https://modrinth.com/mod/chest-tracker)** by
> **[JackFred](https://modrinth.com/user/JackFred)**, updated for Minecraft 26.x. It is **not
> affiliated with or endorsed by JackFred**.
>
> All the original work is his. This fork exists only because the original stopped at Minecraft
> 1.21.4, and the 26.x release line required extensive rewrites.
>
> **If JackFred updates the original to 26.x, use his version instead.**

---

## What it does

Open a chest, close it, and Chest Tracker quietly remembers what was inside. Later, open the
tracker, search for an item, and every container holding it lights up in the world.

- 🔍 **Search your storage** — find any item across every container you've opened
- ✨ **In-world highlighting** — matching containers pulse so you can spot them at a glance
- 🏷️ **Container names** — named containers show their label above them
- 📦 **Looks inside shulker boxes and bundles** — nested items are searchable too
- 🌍 **Per-world memory** — every server and dimension gets its own separate memory
- 🖥️ **Fully client-side** — works on any server, nothing to install server-side

## Requirements

| | |
|---|---|
| **Minecraft** | 26.2 |
| **Loader** | Fabric |
| **Java** | 25 |
| **Required** | [Fabric API](https://modrinth.com/mod/fabric-api), [YACL](https://modrinth.com/mod/yacl) 3.9.0+ |

### Optional integrations

[Searchables](https://modrinth.com/mod/searchables) — autocomplete in the search bar ·
[Mod Menu](https://modrinth.com/mod/modmenu) — config access ·
[Jade](https://modrinth.com/mod/jade) / [WTHIT](https://modrinth.com/mod/wthit) — container contents in tooltips ·
[Shulker Box Tooltip](https://modrinth.com/mod/shulkerboxtooltip) — ender chest preview

---

## ⚠️ Before you install

**Remove these first — they will conflict:**

- **[Where Is It](https://modrinth.com/mod/where-is-it)** — its search and highlighting are now
  built directly into this mod
- **The original Chest Tracker** — this replaces it

Both are enforced: the mod refuses to load alongside them rather than misbehaving quietly.

**Your existing data carries over.** Memory banks and settings are read from the same locations as
the original (`chesttracker/` in your game folder), so upgrading keeps everything you've collected.

---

## Getting started

1. Press <kbd>`</kbd> (grave, above Tab) to open the tracker
2. Open some containers so it has something to remember
3. Search for an item and click it — the GUI closes and the containers holding it light up

Everything is configurable through Mod Menu, including per-memory-bank settings for which
containers to remember, how long memories last, and how results are displayed.

---

## Screenshots

<!-- Replace with your own screenshots before publishing. -->
*Coming soon.*

---

## Known limitations

Honest about what's different from the original:

- **Highlights don't show through walls.** The original disabled depth testing so you could see
  containers hidden behind terrain. Minecraft 26.2 removed the rendering path that made that
  possible, and restoring it needs a custom render pipeline. Planned.
- **Litematica / MaLiLib integration removed** — no 26.x build exists upstream
- **Expanded Storage integration removed** — no 26.x release

## What changed in the port

Minecraft 26.x is the first fully unobfuscated release, and 1.21.9 replaced immediate-mode GUI
drawing with a deferred render-state model. Between 1.21.4 and 26.2 that meant rewriting the entire
GUI layer, the in-world renderer, and all input handling.

Where Is It and JackFredLib are no longer separate downloads — the parts this mod actually used are
built in, so it's one jar instead of three.

Highlights also got some attention: they now **pulse** to be easier to spot, are **translucent** by
default so you can still see the container, and last **12 seconds** instead of 5.

---

## Licence & source

**LGPL-3.0-only**, unchanged from the original.

- **Source:** [github.com/Maticcm/Chest-Tracker-Reloaded](https://github.com/Maticcm/Chest-Tracker-Reloaded)
- **Original:** [github.com/JackFred2/ChestTracker](https://github.com/JackFred2/ChestTracker) by JackFred

Original mod, translations and artwork by JackFred and the Chest Tracker contributors. Please
support the original author — this fork only exists to keep the mod playable on current versions.

## Bug reports

Report issues with **this fork** to
[its issue tracker](https://github.com/Maticcm/Chest-Tracker-Reloaded/issues) — not to JackFred.
He didn't write the 26.x port and shouldn't field its bugs.
