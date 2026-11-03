# Fair Sleep — sleep even with monsters nearby

**Monsters nearby no longer stop you from sleeping.**

*"You may not rest now; there are monsters nearby."* Not any more. Click the bed at night and you
sleep, whatever is lurking in the cave under your floor or behind your wall.

Nothing to install on the client. Fabric and NeoForge, Minecraft 1.21.1, 26.2 and 26.3.
No dependencies.

## The rule

The monster check is the only thing removed. Everything else stays vanilla:

- you sleep at night only (or during a thunderstorm), unless the "Sleep by day" switch is on, in a
  dimension where beds work;
- the bed must be close enough and not obstructed.

Every sleeping block that goes through vanilla's bed check is covered, including those of other
mods that use it.

## Two switches

`/fairsleep settings` (operators) opens a chest with two switches, like every Fair mod. Click an
icon or the pane under it.

**Monsters attack sleepers** (zombie head):

- **green, Allowed** (default): as in vanilla, a monster that reaches you can hit you and wake you
  up;
- **red, Not allowed**: monsters do not target a sleeping player, and the ones already after you
  give up when you fall asleep. Awake again, you are fair game.

**Sleep by day** (clock):

- **red, Off** (default): beds work at night only, as in vanilla;
- **green, On**: beds work by day too, and sleeping by day brings the night (time jumps to
  nightfall, 13000). Sleeping at night still brings the morning. Beds that never work (in the
  Nether, the End) still explode.

Shift-click, number keys and dropping do nothing; nothing leaves the chest.

## Commands

| Command | |
|---|---|
| `/fairsleep settings` | the settings chest (operators) |
| `/fairsleep reload` | reload the config (operators) |

## Config

`config/fairsleep.json`, also written by the settings chest:

| Key | Default | |
|---|---|---|
| `aggroAllowed` | `true` | monsters may attack a sleeping player |
| `daySleep` | `false` | beds work by day, and a day sleep brings the night |

## Building

The Gradle process needs JDK 25, even for the 1.21.1 line (its toolchain, Java 21, is downloaded
automatically).

```bash
./gradlew build                             # default line
./gradlew build -Pminecraft_version=1.21.1  # any line of MATRIX in build.gradle
```

Jars land in `fabric/build/libs/` and `neoforge/build/libs/`.

`.\selftest.ps1` starts a real server per loader and Minecraft line, puts a zombie and a skeleton
right beside a bed, and checks that the player sleeps. The witness replays the scene with the mod
switched off, where vanilla must refuse sleep. It also clicks through the settings chest, and with
aggro off checks that the zombie drops the sleeper and the skeleton cannot pick them; its witness,
aggro allowed, must keep the sleeper as a target. By day, with the day-sleep switch off the bed
must refuse (witness), and with it on the player must fall asleep and stay asleep through their own
ticks, until the switch goes off again. The jump to nightfall is covered by JUnit
(`SleepTimeTest`) and by the mixin, which a server refuses to start without; check it in game.

## License

MIT
