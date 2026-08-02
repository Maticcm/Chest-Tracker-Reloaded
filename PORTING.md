# ChestTracker Reloaded — Porting Notes (1.21.4 → 26.2)

Unofficial fork of [Chest Tracker](https://github.com/JackFred2/ChestTracker) by JackFred, LGPL-3.0-only.
This document records every non-obvious change, so the work is reproducible and reviewable.

**Status:** compiles clean, passes access-widener validation, builds a jar, and **loads successfully
in the dev client** with no mixin failures. Gameplay behaviour still needs manual verification —
see [TESTING.md](TESTING.md).

---

## 1. There is no Minecraft "1.26.x"

Mojang moved to calendar versioning:

| Version | Released |
|---|---|
| 1.21.4 | 2024-12-03 (fork point) |
| 1.21.5 … 1.21.11 | 2025 |
| 26.1, 26.1.1, 26.1.2 | Mar–Apr 2026 |
| **26.2** | 2026-06-16 (target) |

The port was staged on 26.1.2 to validate the toolchain, then moved to 26.2 once it was clear the
optional integrations only publish 26.2 builds.

## 2. The headline change: Minecraft 26.x is unobfuscated

The single most important fact for anyone porting to 26.x.

- Mojang **no longer publishes `client_mappings`** — verified against `piston-meta`: the `downloads`
  block for 26.2 contains only `client` and `server`.
- **Yarn has no 26.x builds** (`meta.fabricmc.net/v2/versions/yarn/26.2` → `[]`).
- Fabric ships a placeholder identity intermediary, `net.fabricmc:intermediary:0.0.0`.

Consequences for the build:

1. **No `mappings(...)` declaration.** `officialMojangMappings()` fails with
   `Failed to find official mojang mappings for 26.2`.
2. **Use the new Loom plugin id `net.fabricmc.fabric-loom`.** The legacy `fabric-loom` id still
   configures remapping and dies with `Configuration 'mappings' has no dependencies`.
3. **`modImplementation` → `implementation`**, `modCompileOnly` → `compileOnly`. No remap step exists.
4. **Access wideners must declare the `official` namespace**, not `named`.

Parchment is dropped — irrelevant now, and it has no 26.x data anyway (stops at 1.21.11).

## 3. Toolchain

| | 1.21.4 (before) | 26.2 (now) |
|---|---|---|
| Gradle | 8.12.1 | **9.5.1** |
| Java | 21 | **25** |
| Loom | `fabric-loom` 1.9-SNAPSHOT | `net.fabricmc.fabric-loom` 1.17-SNAPSHOT |
| Loader | 0.16.10 | 0.19.3 |
| Fabric API | 0.111.0+1.21.4 | 0.156.0+26.2 |
| YACL | 3.6.2+1.21.4 | 3.9.6+26.2-fabric |
| Mappings | Mojang + Parchment | **none (unobfuscated)** |

Gradle 8.14.3 caps at Java 24 and fails on JDK 25 with a bare `25.0.1` error. 9.5.1 is required.
`buildSrc` (upstream's Kotlin release automation) was removed along with the publishing config.

## 4. Class and method renames

Of 105 distinct `net.minecraft` imports, only four failed to resolve — Mojang adopted several
Yarn-style names when unobfuscating.

| 1.21.4 | 26.2 |
|---|---|
| `resources.ResourceLocation` | `resources.Identifier` (167 refs) |
| `net.minecraft.Util` | `net.minecraft.util.Util` |
| `client.renderer.RenderType` | `client.renderer.rendertype.RenderType` (factories on `RenderTypes`) |
| `client.gui.GuiGraphics` | *removed* — see §5 |

Member-level changes found during compilation:

| Old | New |
|---|---|
| `ResourceKey.location()` | `ResourceKey.identifier()` |
| `TagKey.identifier()` | `TagKey.location()` |
| `BlockPos.getCenter()` | `Vec3.atCenterOf(pos)` |
| `ItemStack.getItemHolder()` | `ItemStack.typeHolder()` |
| `ItemStack.SINGLE_ITEM_CODEC` | `ItemStack.CODEC` |
| `ItemContainerContents.nonEmptyStream()` | `nonEmptyItemCopyStream()` |
| `Camera.getPosition()` | `Camera.position()` |
| `GameRenderer.getMainCamera()` | `GameRenderer.mainCamera()` |
| `SharedConstants.getCurrentVersion().getDataVersion().getVersion()` | `SharedConstants.WORLD_VERSION` |
| `Minecraft.screen` / `setScreen` / `getOverlay()` | `Minecraft.gui.screen()` / `gui.setScreen()` / `gui.overlay()` |
| `Screen.hasShiftDown()` (static) | `Minecraft.getInstance().hasShiftDown()` |
| `Minecraft.getToastManager()` | `Minecraft.gui.toastManager()` |
| `Gui.getTabList()` | `Gui.hud.getTabList()` |
| `Minecraft.getGuiSprites()` | `Minecraft.getAtlasManager()` |
| `EditBox.setFormatter(BiFunction)` | `EditBox.addFormatter(TextFormatter)` |
| `ClickEvent.getAction() == Action.X` | `instanceof ClickEvent.SuggestCommand` (sealed interface) |
| `DisplaySlot.teamColorToSlot(colour)` | removed — map by name; `PlayerTeam.getColor()` now returns `Optional<TeamColor>` |
| `CycleButton.builder(fn)` + `.withInitialValue(v)` | `CycleButton.builder(fn, () -> v)` |
| `StringWidget.setColor(int)` | colour the `Component` instead |
| `Items.RED_BED` / `GRAY_DYE` / `BLACK_STAINED_GLASS_PANE` | `Items.BED.pick(DyeColor.RED)` etc. — colour variants are grouped in a `ColorCollection` but remain distinct `Item`s |

Fabric API changes:

| Old | New |
|---|---|
| `client.keybinding.v1.KeyBindingHelper.registerKeyBinding` | `client.keymapping.v1.KeyMappingHelper.registerKeyMapping` |
| `ClientTickEvents.START_WORLD_TICK` / `END_WORLD_TICK` | `START_LEVEL_TICK` / `END_LEVEL_TICK` |
| `HudRenderCallback.EVENT` | `hud.HudElementRegistry.attachElementAfter(...)` |
| `WorldRenderEvents` / `WorldRenderContext` | `level.LevelRenderEvents` / `LevelRenderContext` |
| `Screens.getButtons(screen)` | `Screens.getWidgets(screen)` |
| `ScreenKeyboardEvents.afterKeyPress` `(screen, key, scancode, mods)` | `(screen, KeyEvent)` |

Keybind categories are now registered objects: `KeyMapping.Category.register(id)`, with the label
translation key derived as `key.category.<namespace>.<path>`.

## 5. The GUI rewrite

1.21.9 replaced immediate-mode GUI drawing with a deferred/retained model. `GuiGraphics` is gone;
widgets **extract render state** which `GuiRenderer` later draws.

```java
// 1.21.4
public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
protected void renderWidget(GuiGraphics graphics, ...)

// 26.2
public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick)
protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, ...)
```

`GuiGraphicsExtractor` mirrors most of the old API (`fill`, `blit`, `blitSprite`, `enableScissor`),
so much of the migration was mechanical. Notable differences:

- `drawString` → `text`, `renderItem` → `item`, `renderFakeItem` → `fakeItem`,
  `renderItemDecorations` → `itemDecorations`, `renderTooltip` → `setTooltipForNextFrame`
- `blitSprite`/`blit` now take a `RenderPipeline` first argument (`RenderPipelines.GUI_TEXTURED`)
- `pose()` returns a 2D `Matrix3x2fStack` — `pushPose`/`popPose` → `pushMatrix`/`popMatrix`, and
  **there is no Z**. Z-offset layering is expressed with `nextStratum()` instead.
- `Screen.renderBackground` → `extractBackground`; `AbstractButton.extractWidgetRenderState` is
  `final`, so `Button` subclasses override `extractContents` instead, and label suppression moves
  from `renderString` to `extractDefaultLabel(ActiveTextCollector)`

### Input events

All input handlers moved to record-based events:

| Old | New |
|---|---|
| `mouseClicked(double, double, int)` | `mouseClicked(MouseButtonEvent, boolean)` |
| `mouseReleased(double, double, int)` | `mouseReleased(MouseButtonEvent)` |
| `mouseDragged(double, double, int, double, double)` | `mouseDragged(MouseButtonEvent, double, double)` |
| `keyPressed/keyReleased(int, int, int)` | `keyPressed/keyReleased(KeyEvent)` |
| `charTyped(char, int)` | `charTyped(CharacterEvent)` |
| `onClick(double, double)` | `onClick(MouseButtonEvent, boolean)` |
| `onDrag(double, double, double, double)` | `onDrag(MouseButtonEvent, double, double)` |
| `onPress()` | `onPress(InputWithModifiers)` |

Migrated by rebinding the old parameter names as locals at the top of each body, so method bodies
are unchanged.

### World rendering

26.2 removed `MultiBufferSource` and `LightTexture` outright. World drawing goes through
`SubmitNodeCollector`:

- highlight boxes → `submitCustomGeometry(pose, RenderTypes.debugFilledBox(), renderer)`
- container labels → `submitNameTag(...)`

## 6. Absorbed dependencies

Upstream depended on two other JackFred mods, neither of which has a 26.x build
(Where Is It stops at `2.6.4+1.21.2`; JackFredLib at `0.10.7+1.21.6`). Rather than fork and port
both, the slices actually used were absorbed.

**Where Is It** → `red.jackf.chesttracker.impl.search` (was used across 15 files):
`SearchRequest`, `SearchResult`, `SearchInvoker`, `SearchRequestPopulator`, `ConnectedBlocksGrabber`,
`NestedItemsGrabber`, `ShouldIgnoreKey`, `AnyOfCriterion`, `RenderUtils`, `HighlightRenderer`.

Where Is It made criteria registry-backed and codec-serialisable so searches could run **server-side**.
ChestTracker only ever searches its own client-side memory bank, so the registry, dispatch codecs and
all networking were dropped — a `Criterion` is now just a predicate.

**JackFredLib** → `red.jackf.chesttracker.vendor.jackfredlib` (39 files): `base` (codecs,
`ResultHolder`, `Memoizer`), `gps` (server/world identity — drives memory-bank selection), `toasts`.
Vendored under our own namespace rather than `red.jackf.jackfredlib` so it cannot collide with the
real library if another mod jar-in-jars it. Its `org.apache.http.util.Args` dependency is no longer
on the classpath and was replaced with a three-method local equivalent.

## 7. Runtime fixes found by actually launching the game

These compiled fine and only surfaced at runtime:

1. **`ClientPacketListenerMixin` injection failure.** `PacketUtils.ensureRunningOnSameThread`'s third
   parameter changed from `BlockableEventLoop` to `net.minecraft.network.PacketProcessor`, so the
   `@At` target descriptor no longer matched (`Scanned 0 target(s)`).
2. **`NullPointerException: Components not bound yet`.** 26.x binds item components *after* mod
   client-init runs, so `ItemStack`s can no longer be built in static initialisers or during
   `onInitializeClient`. `DefaultIconsImpl`, `HypixelProvider` and `ItemListWidget` now build their
   stacks lazily, and `DefaultIconsImpl.registerDeferred(...)` queues icon registrations until the
   list is first accessed (used by `ShareEnderChestIntegration`).
3. **`GuiAccessor` mixin failure.** `SCORE_DISPLAY_ORDER` moved from `Gui` to `Hud`, alongside the
   tab list. The vendored GPS accessor was retargeted.
4. **`StackOverflowError` on opening any GUI containing a button.** `AbstractButton`'s
   `extractWidgetRenderState` is `final` and its body is exactly
   `extractContents(...); handleCursor(...)`. When the `Button` subclasses moved from
   `renderWidget` to `extractContents`, their bodies still called
   `super.extractWidgetRenderState(...)` — which dispatches straight back into `extractContents`
   and recurses until the stack blows. Affected `HoldToConfirmButton` and `ItemButton`; the correct
   call for "draw the vanilla button background" is `extractDefaultSprite(graphics)`.

   **This compiled cleanly and survived client startup**, because buttons only render once a screen
   is opened. It is the canonical example of why §9 below matters.

## 8. Breaking changes for users

- **Where Is It must be uninstalled.** Its search/highlight is now built in; `fabric.mod.json`
  declares `breaks` on both `whereisit` and the original `chesttracker`.
- **Litematica / MaLiLib integration removed** — no 26.x builds; the bundled `libs/` jars were 1.21.3.
  This drops the material-list search buttons.
- **Expanded Storage integration removed** — no 26.x release.
- The Where Is It config button is gone from the config screen; its tunables now live in
  `impl/search/SearchConfig` with upstream's defaults.
- Toast padding is fixed at the default — 26.x reworked GUI sprite lookup and no longer exposes
  per-sprite nine-slice metrics at that call site.
- Mod id is now `chesttrackerreloaded`; the asset namespace stays `chesttracker`.

**Data is preserved.** Memory banks still load from `<game dir>/chesttracker/` and config from
`<config dir>/chesttracker.json5` — both unchanged. The one storage-format risk is memory-key icons,
which moved from `ItemStack.SINGLE_ITEM_CODEC` to `ItemStack.CODEC`; the existing
`ITEM_STACK_IGNORE_COUNT` fallback should still read old files, but verify icons after upgrading
(see TESTING.md §6).

## 9. What is not verified

The build is green and the client loads cleanly, but **very little gameplay has been exercised**.
Rendering output, search results, data migration against a real 1.21.4 profile, and multiplayer
behaviour all need a human. [TESTING.md](TESTING.md) is the checklist, ordered by risk.

A caution learned the hard way: "compiles and the client reaches the main menu" says **nothing**
about GUI correctness. The `StackOverflowError` in §7.4 passed both of those gates and still broke
every screen in the mod the moment one was opened. Treat the GUI and renderer sections of
TESTING.md as genuinely unverified until someone has clicked through them.
