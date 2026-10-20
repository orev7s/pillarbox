# PillarBox

Per-app 4:3 aspect engine for wide Android tablets.

Pick the games that were designed around an iPad Pro viewport, and PillarBox makes those apps run inside a real 4:3 viewport on a 16:10 or 3:2 tablet — then puts everything back the moment you leave the app.

```
Pixel count unchanged. Viewport ratio changed. FOV math matches the iPad build.
```

---

## Why

The iPad Pro is 4:3. Games are tuned against it: vertical FOV, HUD anchors, minimap position, menu column widths. A 16:10 or 3:2 Android tablet gives the renderer a wider viewport, so the same game either stretches the horizontal FOV, pulls the camera back, or reflows the HUD into a shape it was never designed for.

PillarBox gives selected apps the geometry they expect, without touching the panel.

## What it actually does

PillarBox is not a hook and it does not patch game memory. It changes the logical display geometry of the device while a selected app is in front:

1. A root daemon watches the top resumed activity once per second.
2. When the package is in `config.json`, it computes the largest 4:3 rectangle that fits your panel, aligned to 8 px, plus a proportionally scaled density.
3. It applies `wm size <w>x<h>` and `wm density <dpi>`.
4. Launcher, system UI, screen off, or any app not in the list → `wm size reset` and `wm density reset`.

Because the override is the real display configuration, every consumer of `DisplayMetrics` and `Configuration` sees 4:3 — the renderer, the layout engine, and the game's own aspect detection. That is the difference between a real 4:3 viewport and a spoof that only lies to one API and produces a distorted image.

## What you get, and what you don't

| | |
|---|---|
| Real 4:3 render target | Yes — the app renders 4:3 natively |
| iPad-style FOV and HUD anchoring | Yes |
| Extra screen real estate | No — 4:3 inside a wider panel means pillarbox bars |
| Panel change | None |
| Per-app granularity | Yes, list-based |
| System-wide side effects | Only while a selected app is foreground; the display geometry is restored on exit |

Expect a short resize animation when the engine switches, and a briefly resized system UI if you pull down the shade while a game is framed. That is the cost of a real viewport change instead of a fake one.

Games that cache display metrics at launch should be restarted once after being added.

## Install

Requirements: rooted tablet (Magisk or KernelSU), Android 11+.

1. Flash `PillarBox-Module-v1.0.0.zip` in Magisk / KernelSU, reboot.
2. Install `PillarBox-1.0.0.apk`.
3. Open it, grant superuser.
4. **Add apps**, pick the games, choose a ratio (4:3 by default), done.

The APK also works without the module: it deploys the same engine to `/data/local/tmp/pillarbox` and runs it from there. The module only makes it survive reboots from `/data/adb/pillarbox`.

## Ratios

`4:3` is the one you want for iPad parity. The list also carries `16:10`, `16:9`, `5:4`, `3:2` and `1:1` for experiments — clipping a game to a narrower viewport is sometimes useful for competitive framing.

## Config

The app writes this; you can also edit it by hand.

```json
{
  "enabled": true,
  "apps": {
    "com.mojang.minecraftpe": "4:3",
    "com.tencent.ig": "4:3",
    "com.miHoYo.GenshinImpact": "16:10"
  }
}
```

Path: `/data/adb/pillarbox/config.json` (module) or `/data/local/tmp/pillarbox/config.json` (standalone).

## Engine CLI

```sh
su -c "/data/adb/pillarbox/pillarboxd start"
su -c "/data/adb/pillarbox/pillarboxd status"
su -c "/data/adb/pillarbox/pillarboxd stop"
```

`stop` also restores the display. Logs land in `engine.log` next to the config, capped at 128 KB.

## Build

Windows, JDK 21, Android SDK with platform 34, Gradle 8.11.1.

```powershell
.\build.ps1
```

`build.ps1` normalises line endings, stages the daemon into the APK assets, runs `:app:assembleRelease`, and packages `dist/PillarBox-1.0.0.apk` plus `dist/PillarBox-Module-v1.0.0.zip`.

Manual equivalent:

```powershell
$env:JAVA_HOME="C:\path\to\jdk-21"
gradle -p . :app:assembleRelease --no-daemon
```

The release keystore lives in `signing/pillarbox.jks` with the password `pillarbox`. It is published on purpose: forks should be able to produce an APK with the same signature so updates install over the official build.

## Layout

```
app/                     Compose + Material 3 companion app
  data/                  root bridge, config model, engine control, app inventory
  ui/                    home screen, ratio sheet, app picker, theme
module/                  Magisk / KernelSU package
  bin/pillarboxd         the engine (POSIX sh, no dependencies)
  service.sh             boot start
  customize.sh           installs the engine into /data/adb/pillarbox
  uninstall.sh           stops the engine and restores the display
```

## Notes and limits

- Root is required. `wm` needs shell/root and the daemon needs to survive the app being swiped away.
- Some ROMs animate or re-layout more aggressively on display change; HyperOS and One UI both behave, AOSP is the cleanest.
- Anti-cheat: this is a developer-options display-size change, not memory injection, not a hook. It is still a non-standard display configuration — decide for yourself how your game's anti-cheat feels about that.
- Not tested on foldables. The daemon reads the physical size at runtime, so it should follow the panel, but folding mid-session is untested.

## License

MIT. Fork it, ship it, break it.
