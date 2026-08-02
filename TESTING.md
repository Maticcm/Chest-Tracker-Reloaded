# ChestTracker Reloaded — Manual Test Script

The port is verified to **compile, pass access-widener validation, build a jar, and load in the dev
client**. Everything below needs a human at the keyboard, because it depends on world interaction,
rendering output, and a real multiplayer server.

Run these against Minecraft **26.2** with Fabric API + YACL installed. Tick each box.

> **Before you start:** back up `<game dir>/chesttracker/` and `<config dir>/chesttracker.json5`.
> Several tests deliberately exercise existing data.

---

## 1. Startup and loading  ← already verified in the dev client

These four were confirmed during porting (`./gradlew runClient`, offline session). Re-check only if
something changes.

- [x] Client reaches the main menu with no crash — held a stable title screen for minutes
- [x] `Loading ChestTracker` appears in the log
- [x] Mixin subsystem reports `Compatibility level set to JAVA_25`, no `Critical injection failure`
      and no `failed injection check`
- [x] `chesttrackerreloaded` is listed in the resource manager reload

Still to check with a real (non-offline) session:

- [ ] Mod appears in Mod Menu as **ChestTracker Reloaded**
- [ ] Config screen opens from Mod Menu without error

> Note: the dev run logs 401s from `authlib` and Realms. Those are the offline dev session, not the
> mod — ignore them.

**Why it matters:** the mod applies 10 mixins plus 4 vendored GPS mixins and 3 access-widener
entries. `ClientPacketListenerMixin` in particular targets an invoke site whose descriptor changed
in 26.x, and mixin failures are silent until the class is first loaded.

## 2. Keybinds

- [ ] `` ` `` (GRAVE) opens the ChestTracker GUI from the world
- [ ] `` ` `` opens the GUI while a chest screen is open
- [ ] The keybind appears under a **ChestTracker Reloaded** category in Options → Controls
- [ ] Typing in a search box or anvil does **not** trigger the keybind

**Why it matters:** 26.x replaced string keybind categories with registered `KeyMapping.Category`
objects, and `KeyMapping.matches` now takes a `KeyEvent`.

## 3. Chest tracking (single-player)

- [ ] Place a chest, put items in, close it — items are remembered
- [ ] Reopen the GUI and confirm the items and container are listed
- [ ] Break the chest — the memory is removed
- [ ] Double chest: both halves resolve to **one** memory, not two
- [ ] Ender chest contents are tracked separately
- [ ] Shulker box inside a chest — its contents are found by search (nested search)
- [ ] Barrel, hopper, dispenser, dropper all track correctly

**Why it matters:** the connected-block and nested-item logic was reimplemented from Where Is It
rather than ported wholesale.

## 4. Searching and highlighting  ← highest risk area

- [ ] Search an item in the GUI; matching containers highlight in-world
- [ ] The highlight box renders as a **coloured cube**, correctly positioned on the container
- [ ] The highlight **fades out** over a few seconds and then disappears
- [ ] Highlights are visible through terrain as expected
- [ ] Container **name labels** render above highlighted containers
- [ ] Labels are billboarded (always face the camera) and readable at distance
- [ ] Labels are sorted correctly — nearer labels draw over further ones
- [ ] Shift-click a remembered item to search precisely (component match)
- [ ] Searching from a container screen highlights the hovered item's locations
- [ ] Move far from the origin (e.g. x=100000) and confirm highlights still land accurately

**Why it matters:** this is the most heavily rewritten code. 26.2 deleted `MultiBufferSource`,
`LightTexture` and `WorldRenderEvents` outright, so boxes are now submitted as custom geometry and
labels as name tags through `SubmitNodeCollector`. The far-from-origin check exercises the
camera-relative translation that avoids float precision loss.

## 5. GUI

- [ ] Main GUI renders correctly — background, borders, item grid
- [ ] Scrolling works via scrollbar drag and mouse wheel
- [ ] Resize handle (bottom-right) resizes the GUI and persists across reopen
- [ ] Search bar accepts input, and autocomplete appears (needs Searchables)
- [ ] Item tooltips show on hover
- [ ] Memory-bank selector opens and switches banks
- [ ] Edit Memory Bank screen: every settings tab renders
- [ ] **Cycle buttons** on those tabs show the correct current value and cycle properly
- [ ] Hold-to-confirm delete button fills and fires only after the hold
- [ ] Inventory button appears on container screens and can be **dragged** to reposition
- [ ] Inventory button position persists after reopening

**Why it matters:** all 17 widget/screen classes moved to the extract-render-state model, mouse
handling moved to `MouseButtonEvent`, and the `CycleButton` builder no longer has
`withInitialValue` — the initial value is now supplied up front, which is easy to get subtly wrong.

## 6. Data storage and migration  ← do this with a real pre-existing profile

- [ ] An existing `chesttracker/` directory from **1.21.4** loads without error
- [ ] Previously remembered containers still appear with correct items and names
- [ ] Existing `chesttracker.json5` config is read; settings match what you had
- [ ] Creating a new memory bank works
- [ ] Deleting a memory bank works
- [ ] Renaming a memory bank works
- [ ] Restart the client and confirm everything persisted

**Why it matters:** storage paths are unchanged and the codec layer was not restructured, but
`ItemStack.SINGLE_ITEM_CODEC` was removed and replaced with `ItemStack.CODEC` for memory-key icons.
Old files should still read; **verify icons specifically**.

## 7. Multiplayer

- [ ] Join a dedicated server; memories are stored per-server
- [ ] Memories persist across a disconnect/reconnect
- [ ] Switching dimensions (Nether/End) keys memories separately
- [ ] Respawning after death does not lose or corrupt memories
- [ ] Two different servers keep separate memory banks
- [ ] Hypixel (if you have access): SkyBlock ender chest / backpack / sack detection

**Why it matters:** server/world identity comes from the vendored JackFredLib **GPS** module, which
reads the scoreboard and tab list through mixins. Its `DisplaySlot.teamColorToSlot` call no longer
exists in 26.2 and was replaced with a name-based lookup — worth confirming on a server that uses
team colours. The respawn path also exercises the fixed `ClientPacketListenerMixin`.

## 8. Mod integrations

- [ ] **Searchables** — autocomplete dropdown in the search bar, with syntax colouring
- [ ] **Jade** — chest contents preview in the Jade tooltip
- [ ] **WTHIT** — chest contents preview in the WTHIT tooltip
- [ ] **Shulker Box Tooltip** — ender chest preview
- [ ] **Mod Menu** — config screen reachable

**Why it matters:** Jade's element API was renamed (`IElement`→`Element`,
`IElementHelper`→`JadeUI`), and the Searchables text formatter moved from `setFormatter` to
vanilla's `EditBox.addFormatter`.

## 9. Known-removed features (confirm they are absent, not broken)

- [ ] No Litematica material-list search buttons — and **no crash** with Litematica absent
- [ ] No Expanded Storage container support
- [ ] No "Where Is It settings" button in the config screen
- [ ] Toast notifications still appear (they use the vendored toast code, with default padding)

---

## Reporting a failure

Include: the checklist item, the full log from `run/logs/latest.log`, any crash report from
`run/crash-reports/`, and your exact Minecraft / Fabric API / YACL versions.
