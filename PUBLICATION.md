# Fair Sleep 0.1.0 — publication

## 1. Modrinth : créer le projet

https://modrinth.com/dashboard/projects → *Create a project*

| Champ | Valeur |
|---|---|
| Name | `Fair Sleep — Sleep Despite Monsters & Day` |
| URL (slug) | `fair-sleep` |
| Visibility | Public |
| Summary | `Sleep even when monsters are nearby, and by day if you want. Operators choose from a chest menu whether monsters attack sleepers and whether beds work by day. Server-side only!` |
| Categories | Game Mechanics, Utility |
| Environments | Client : **Unsupported** · Server : **Required** |
| License | MIT |
| Source code | `https://github.com/BackupBackupFede/fair-sleep` |
| Issues | `https://github.com/BackupBackupFede/fair-sleep/issues` |
| Donation | Ko-fi : `https://ko-fi.com/acesoverdeuces777` |
| Icon | `fair-sleep\common\src\main\resources\icon.png` |
| Description | le bloc « Description » plus bas |

Après création : noter l'**ID du projet** (Settings → General, ou l'URL de l'API) pour la publication par tag.

## 2. CurseForge : créer le projet

https://authors.curseforge.com/ → *Create Project* → Minecraft → Mods

| Champ | Valeur |
|---|---|
| Name | `Fair Sleep — Sleep Despite Monsters & Day` |
| Summary | `Sleep even when monsters are nearby, and by day if you want. Operators choose from a chest menu whether monsters attack sleepers and whether beds work by day. Server-side only!` |
| Main category | Server Utility |
| Other categories | Miscellaneous |
| License | MIT License |
| Source / Issues | les mêmes liens GitHub |
| Logo | même icône |
| Description | choisir l'éditeur **Markdown**, coller le bloc « Description » |

Après création : noter le **Project ID** (colonne de droite de la page du projet).

## 3. Description (Markdown, la même pour les deux, dans le style de Fair Death)

```markdown
**Sleep even when monsters are nearby, and by day if you want.**

## Vanilla
Server-side only

## Chest menu and operators
`/fairsleep settings` opens a chest. Green: allowed. Red: not allowed. Click to switch.

| Switch | Covers |
|---|---|
| Monsters attack sleepers | Green (default): a monster can still hit you and wake you up. Red: monsters ignore sleeping players, and the ones chasing you give up. |
| Sleep by day | Red (default): night only, as in vanilla. Green: beds work by day too, and sleeping by day brings the night. |

## Fix
- Monsters nearby never stop you from sleeping.
- Everything else stays vanilla: bed close enough, not obstructed, night only (unless Sleep by day is on).
- Sleeping at night still brings the morning. Beds still explode in the Nether and the End.

## Commands
- `/fairsleep settings` - the switches (operators).
- `/fairsleep reload` - reload the config (operators).

## Config
`config/fairsleep.json` - `aggroAllowed`, `daySleep`.

Enjoying the mod? [Buy me a coffee on Ko-fi](https://ko-fi.com/acesoverdeuces777)
```

## 4. Les fichiers

Par tag (section 5) : rien à faire, le workflow publie les 6.
À la main : une version par jar.

| Fichier (dans `fair-sleep\`) | Loader | Minecraft | Numéro de version |
|---|---|---|---|
| `fabric\build\libs\fairsleep-fabric-26.3-0.1.0.jar` | Fabric | 26.3 | `0.1.0+26.3-fabric` |
| `fabric\build\libs\fairsleep-fabric-26.2-0.1.0.jar` | Fabric | 26.2 | `0.1.0+26.2-fabric` |
| `fabric\build\libs\fairsleep-fabric-1.21.1-0.1.0.jar` | Fabric | 1.21.1 | `0.1.0+1.21.1-fabric` |
| `neoforge\build\libs\fairsleep-neoforge-26.3-0.1.0.jar` | NeoForge | 26.3 | `0.1.0+26.3-neoforge` |
| `neoforge\build\libs\fairsleep-neoforge-26.2-0.1.0.jar` | NeoForge | 26.2 | `0.1.0+26.2-neoforge` |
| `neoforge\build\libs\fairsleep-neoforge-1.21.1-0.1.0.jar` | NeoForge | 1.21.1 | `0.1.0+1.21.1-neoforge` |

Canal Release, aucune dépendance.

Changelog :

```
First release.
- Monsters nearby no longer stop you from sleeping.
- /fairsleep settings (operators): two switches.
  - "Monsters attack sleepers" (allowed by default, as in vanilla).
  - "Sleep by day": beds work by day too, and sleeping by day brings the night (off by default).
```

## 5. Publication par tag (GitHub Actions)

Le workflow `.github/workflows/release.yml` publie les 6 versions sur Modrinth et CurseForge, plus une
release GitHub, quand on pousse le tag `v0.1.0`. Il lui faut, dans le dépôt GitHub
(*Settings → Secrets and variables → Actions*) :

| Nom | Type | Qui |
|---|---|---|
| `MODRINTH_ID` | Variable | posée (`6sYB95vR`) |
| `CURSE_ID` | Variable | posée (`1782063`) |
| `MODRINTH_TOKEN` | Secret | posé |
| `CURSE_TOKEN` | Secret | posé |

Une plateforme sans son secret est simplement sautée.
