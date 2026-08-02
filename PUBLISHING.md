# Publishing ChestTracker Reloaded

Release checklist for the fork. **Read §1 before your first upload** — the licence obligations are
not optional, and getting them wrong on a first release is awkward to walk back.

- **Project name (storefront):** ChestTracker Reloaded
- **In-game name:** Chest Tracker
- **Mod id:** `chesttrackerreloaded`
- **Licence:** LGPL-3.0-only
- **Repo:** https://github.com/Maticcm/Chest-Tracker-Reloaded

---

## 1. Licence obligations — required, not optional

This is a derivative of three LGPL-3.0-only projects by JackFred (Chest Tracker, Where Is It,
JackFredLib). Publishing it means you must:

- [x] **Keep the licence LGPL-3.0-only.** You cannot relicense it, including to something more
      permissive. `LICENSE` is unchanged and is bundled inside the jar.
- [ ] **Publish the source.** LGPL requires recipients can obtain the source of what you shipped.
      Push this repo public at the URL above **before** uploading the jar anywhere.
      A private repo with a public jar is a licence violation.
- [x] **Preserve attribution.** JackFred is credited in `fabric.mod.json` authors, the description,
      README, and per-file headers on adapted code. Do not remove these.
- [x] **State it is unofficial.** The description and README both say so explicitly.

If you ever accept outside contributions, they inherit LGPL-3.0-only too.

## 2. Naming — why the two names differ

The storefront project is **ChestTracker Reloaded**; the mod calls itself **Chest Tracker** in game.

That split is deliberate. Publishing a fork under a name identical to the original invites
takedown under Modrinth's and CurseForge's rules on duplicate/impersonating projects, and users
can't tell the two apart in search results. Keeping the *listing* distinct avoids that, while the
in-game name stays as you wanted. There is no in-game ambiguity because `fabric.mod.json` declares
`breaks` on the original `chesttracker`, so the two can never be installed together.

**Do not** rename the storefront listing to plain "Chest Tracker".

## 3. Pre-flight

- [ ] Repo pushed public at https://github.com/Maticcm/Chest-Tracker-Reloaded
- [ ] `git remote -v` points at your fork, **not** JackFred's (see §7)
- [ ] `mod_version` in `gradle.properties` bumped
- [ ] Changelog written for the version (`changelogs/<version>.md`)
- [ ] `./gradlew clean build` passes
- [ ] Manual test pass done — see [TESTING.md](TESTING.md). **The GUI and renderer sections are the
      ones that matter**; every bug found so far has been in exactly those.

## 4. Build

```bash
./gradlew clean build
```

Produces in `build/libs/`:

| File | Upload? |
|---|---|
| `chesttrackerreloaded-<version>+26.2.jar` | yes — this is the mod |
| `chesttrackerreloaded-<version>+26.2-sources.jar` | recommended — helps satisfy §1 |

Do **not** upload `-dev` or `-sources` jars as the primary file.

## 5. Listing metadata

- **Minecraft:** 26.2 · **Loader:** Fabric · **Environment:** client-side only
- **Java:** 25
- **Icon:** `src/client/resources/assets/chesttracker/icon.png` (256×256 PNG)

**Dependencies — required:**
- Fabric API
- YACL ≥ 3.9.0

**Optional:** Searchables (search autocomplete), Mod Menu, Shulker Box Tooltip, WTHIT, Jade

**Incompatible — state this prominently:**
- **Where Is It** — its search/highlight is built in; both installed will conflict
- **Chest Tracker** (the original) — same mod, declared via `breaks`

**Suggested description opening:**

> An unofficial fork of JackFred's Chest Tracker, updated for Minecraft 26.x. Not affiliated with
> or endorsed by JackFred. If the original updates to 26.x, use it instead.

## 6. Known limitations to disclose

Be upfront about these; they are user-visible regressions versus the original:

- **Litematica / MaLiLib integration removed** — no 26.x build exists upstream
- **Expanded Storage integration removed** — no 26.x release
- **Highlights do not show through walls.** Upstream disabled depth testing so hidden containers
  were visible through terrain. 26.2 has no ready-made no-depth filled-box render type, so this
  needs a custom `RenderPipeline` and is not yet done. This is the most likely thing users will
  report as "broken" versus the original — mention it rather than field bug reports.

See [PORTING.md](PORTING.md) for the full change list.

## 7. Fix the git remote first

`origin` currently still points at the upstream repo, which this fork was cloned from:

```
origin  https://github.com/JackFred2/ChestTracker
```

A `git push` would attempt to push your fork's history to JackFred's repository. Retarget it before
doing anything with git:

```bash
git remote set-url origin https://github.com/Maticcm/Chest-Tracker-Reloaded
git remote -v   # verify
```

Optionally keep upstream for merging future fixes:

```bash
git remote add upstream https://github.com/JackFred2/ChestTracker
```

## 8. Deliberately not automated

Upstream's release automation (GitHub Releases, Modrinth, CurseForge via `mod-publish-plugin`) was
**removed**, along with `buildSrc`. It was hardcoded to JackFred's project IDs — Modrinth
`ni4SrKmq`, CurseForge `397217` — so leaving it in place risked a stray `RELEASE=1` build uploading
this fork to the original author's project pages.

Uploads are therefore manual. If you later want automation, wire it to **your own** project IDs and
supply tokens via environment variables, never committed.
