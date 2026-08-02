# ChestTracker Reloaded

**An unofficial fork of [Chest Tracker](https://github.com/JackFred2/ChestTracker) by JackFred, updated for Minecraft 26.x.**

Maintained at [Maticcm/Chest-Tracker-Reloaded](https://github.com/Maticcm/Chest-Tracker-Reloaded). In game the mod is simply called **Chest Tracker**; "Reloaded" distinguishes this fork's project listing from the original.

A client-side mod to remember where you've put items. Press the **GRAVE** key ``` ` ``` to open the GUI,
and search from any inventory to highlight the containers holding an item.

---

## Attribution

This project is a fork and is **not affiliated with or endorsed by JackFred**.

All original work is by **JackFred** ([JackFred2](https://github.com/JackFred2)) and the Chest Tracker
contributors and translators. This fork exists only because upstream stopped at Minecraft 1.21.4 and
the 26.x release line required extensive changes.

It incorporates code from three of JackFred's projects, all LGPL-3.0-only:

| Project | Used for | Upstream |
|---|---|---|
| Chest Tracker | the mod itself | [JackFred2/ChestTracker](https://github.com/JackFred2/ChestTracker) |
| Where Is It | search & in-world highlighting (absorbed) | [JackFred2/WhereIsIt](https://github.com/JackFred2/WhereIsIt) |
| JackFredLib | codecs, GPS, toasts (vendored) | [JackFred2/JackFredLib](https://github.com/JackFred2/JackFredLib) |

Adapted files carry a header noting what they were derived from and what changed.

If JackFred resumes upstream development for 26.x, **use the official mod instead**.

## License

**LGPL-3.0-only**, unchanged from upstream — see [LICENSE](LICENSE). As an LGPL derivative this fork
must remain LGPL-3.0-only, and the original copyright notices are retained.

## Requirements

- Minecraft **26.2**
- Java **25**
- [Fabric Loader](https://fabricmc.net/) ≥ 0.19.0
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [YACL](https://modrinth.com/mod/yacl) ≥ 3.9.0

### Optional integrations

[Searchables](https://modrinth.com/mod/searchables) (search autocomplete) ·
[Mod Menu](https://modrinth.com/mod/modmenu) ·
[Shulker Box Tooltip](https://modrinth.com/mod/shulkerboxtooltip) ·
[WTHIT](https://modrinth.com/mod/wthit) ·
[Jade](https://modrinth.com/mod/jade)

## ⚠️ Upgrading from Chest Tracker

**Remove Where Is It before installing.** Its search and highlighting are now built in, and running
both will conflict. `ChestTracker Reloaded` also declares a `breaks` on the original `chesttracker`
mod id, so remove that too.

**Your data is preserved.** Memory banks are still read from `<game dir>/chesttracker/` and the
config from `<config dir>/chesttracker.json5` — both paths are unchanged, so existing memories and
settings carry over untouched.

Two integrations are **removed** because they have no 26.x builds upstream:

- **Litematica / MaLiLib** — material-list search buttons
- **Expanded Storage** — extra container support

See [PORTING.md](PORTING.md) for the full list of changes and the technical detail behind them.

## Building

```bash
./gradlew build
```

Output lands in `build/libs/`. Requires JDK 25.
