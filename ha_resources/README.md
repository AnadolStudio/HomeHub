# ha_resources

Android-library module that ships the **full Material Design Icons (MDI)** set
used by Home Assistant frontend, packaged as Android `VectorDrawable` resources
plus a generated type-safe Kotlin mapping `mdi:* -> @DrawableRes Int`.

- MDI version: **7.4.47** (matches `@mdi/svg` in home-assistant/frontend)
- Icons in this build: **7447**
- Generated at: 2026-05-15T19:34:54.891Z

## Layout

```
ha_resources/
├── build.gradle.kts                 # android-library
├── package.json                     # @mdi/svg@7.4.47
├── scripts/download-ha-icons.mjs    # generator
├── ha-icons/                        # dev metadata (in git)
│   ├── icons_index.json
│   └── ha_domain_fallback_icons.json
├── svg/                             # raw SVG (gitignored; regenerated on each run)
└── src/main/
    ├── AndroidManifest.xml
    ├── res/drawable/                # ic_mdi_<name>.xml  (in git)
    └── java/com/anadolstudio/ha_resources/
        ├── HaIcon.kt
        ├── HaIcons.kt                # generated
        └── HaDomainFallbackIcons.kt  # generated
```

## Regenerate everything

```
cd ha_resources
npm install
npm run download:ha-icons
```

The script:

1. Verifies `@mdi/svg` is installed at version `7.4.47`.
2. Copies all SVGs to `svg/` (gitignored, kept only for local inspection).
3. Converts each `<path d="..."/>` into an Android `VectorDrawable`
   (24dp/24dp, viewport 24/24, `fillColor="@android:color/black"`) in
   `src/main/res/drawable/ic_mdi_<sanitized-name>.xml`.
4. Writes `ha-icons/icons_index.json` (full `haIcon → drawable` index).
5. Writes `ha-icons/ha_domain_fallback_icons.json` (HA domain → `mdi:*` fallback).
6. Regenerates `HaIcons.kt` and `HaDomainFallbackIcons.kt`.
7. Verifies that the required icons exist (`mdi:robot`, `mdi:robot-off`,
   `mdi:lightbulb`, `mdi:palette`, `mdi:script-text`).

## Use

```kotlin
import com.anadolstudio.ha_resources.HaIcons
import com.anadolstudio.ha_resources.HaDomainFallbackIcons

val drawableRes: Int? = HaIcons.resolve("mdi:robot")
val fallbackIcon: String? = HaDomainFallbackIcons.resolve("automation") // -> "mdi:robot"
```

Hook into your `:app`:

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":ha_resources"))
}
```

## Name mapping rules

| MDI id          | Drawable file              | Drawable name           |
|-----------------|----------------------------|-------------------------|
| `mdi:robot`     | `ic_mdi_robot.xml`         | `ic_mdi_robot`          |
| `mdi:robot-off` | `ic_mdi_robot_off.xml`     | `ic_mdi_robot_off`      |
| `mdi:numeric-1` | `ic_mdi_numeric_1.xml`     | `ic_mdi_numeric_1`      |

Hyphens become underscores; everything else lower-cases. Prefix is always `ic_mdi_`.

## Important

Do **not** edit `HaIcons.kt`, `HaDomainFallbackIcons.kt`, `icons_index.json`,
or any `ic_mdi_*.xml` by hand — they will be overwritten on the next run.
