# Fair Sleep — test client

## Les jars

| Version | Fabric | NeoForge |
|---|---|---|
| **26.3** | `C:\Users\FLO\Projets\MINECRAFT\fair-sleep\fabric\build\libs\fairsleep-fabric-26.3-0.1.0.jar` | `...\neoforge\build\libs\fairsleep-neoforge-26.3-0.1.0.jar` |
| 26.2 | `...\fabric\build\libs\fairsleep-fabric-26.2-0.1.0.jar` | `...\neoforge\build\libs\fairsleep-neoforge-26.2-0.1.0.jar` |
| **1.21.1** | `...\fabric\build\libs\fairsleep-fabric-1.21.1-0.1.0.jar` | `...\neoforge\build\libs\fairsleep-neoforge-1.21.1-0.1.0.jar` |

Après une modification du code : `.\build-all.ps1` dans `fair-sleep`, puis reprendre le jar.

## Quoi tester où

| Combinaison | Pourquoi | Test |
|---|---|---|
| **Fabric 26.3** | la plus jouée | tout |
| **NeoForge 1.21.1** | les gros modpacks ; la vérification du lit y passe par un autre chemin, à vérifier en vrai | tout |
| les 4 autres | — | 1, 7, 8 et 12 seulement |

## Le serveur

Mêmes serveurs que Fair Loot, les deux mods y sont déjà :

| Serveur | Dossier (`C:\Users\FLO\Projets\MINECRAFT\_serveurs-test\…`) | Client à lancer |
|---|---|---|
| Fabric 26.3 | `fabric-26.3\` | 26.3 vanilla |
| NeoForge 1.21.1 | `neoforge-1.21.1\` | 1.21.1 vanilla |

1. Première fois seulement : dans `eula.txt`, `eula=true` si tu acceptes le contrat de Minecraft.
2. Double-clic sur `start.bat`, attendre `Done`.
3. Fenêtre du serveur : `op <ton pseudo>` (une fois connecté). Si « not white-listed » :
   `whitelist off`.
4. Launcher → la version vanilla correspondante → Connexion directe → `localhost`.
5. Fini : `stop`. Un seul serveur à la fois (même port).

Toujours **de nuit** (`/time set night`) et **en survie** (`/gamemode survival`) : en créatif,
vanilla laisse toujours dormir. Un zombie qui ne bouge pas : `/summon zombie ~ ~ ~ {NoAI:1b}`.

## Tests

- [ ] **1. Zombie collé au lit**, à découvert : on dort. Aucun message « You may not rest now ».
- [ ] **2. Sans le mod** (retirer le jar, redémarrer), même scène : vanilla refuse, « You may not
      rest now; there are monsters nearby ». Remettre le jar.
- [ ] **3. Plusieurs monstres** autour du lit (zombie, squelette, araignée, creeper) : on dort.
- [ ] **4. Le reste de la règle vanilla tient** : de jour, refus « You can sleep only at night… » ;
      trop loin du lit, « You may not rest now; the bed is too far away ».
- [ ] **5. Réglage par défaut, un vrai zombie (sans `NoAI`) arrive pendant le sommeil** : il peut
      te frapper et te réveiller, comme en vanilla. Seul : `/gamerule players_sleeping_percentage 200`
      (`playersSleepingPercentage` en 1.21.1) pour rester couché sans que la nuit passe.
- [ ] **6. Créatif** : on dort, comme en vanilla.
- [ ] **7. `/fairsleep settings`** (op) : coffre « Fair Sleep - while you sleep ». Lit rouge en haut
      (« Fixed rule »). Ligne du milieu : tête de zombie (« Monsters attack sleepers »), panneau
      **vert** « Allowed (vanilla) » dessous ; horloge (« Sleep by day »), panneau **rouge**
      « Off (vanilla) » dessous. Un clic sur une icône **ou** son panneau la bascule, l'autre ne
      bouge pas. Shift-clic, touche numérique, `Q` : rien ne bouge, rien ne sort du coffre.
- [ ] **8. Panneau rouge, même scène que le test 5** : le zombie ne s'approche pas pour frapper,
      ne t'attaque pas. Réveillé, il t'attaque de nouveau.
- [ ] **9. Un zombie te poursuit, tu te couches** (panneau rouge) : il te lâche dès que tu dors.
- [ ] **10. Le réglage tient** : `config\fairsleep.json` contient `"aggroAllowed": false` ;
      `stop`, redémarrage, `/fairsleep settings` : toujours rouge. Un joueur non-op ne voit pas
      `/fairsleep`.
- [ ] **11. Horloge rouge (défaut), de jour** (`/time set day`, `/weather clear`) : le lit refuse,
      comme en vanilla (« You can sleep only at night… »).
- [ ] **12. Horloge verte, de jour** : on se couche, on **reste couché**, et au bout de ~5 s la
      **nuit tombe** (`/time query daytime` ≈ 13000). Pas de saut au lendemain matin.
- [ ] **13. Horloge verte, de nuit** : dormir amène le **matin**, comme en vanilla.
- [ ] **14. Horloge verte, dans le Nether** : le lit explose toujours.
