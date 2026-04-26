# App de révisions par blocage

## 1. Vision

Une application Android qui force des révisions actives en affichant un QCM avant l'ouverture de certaines applications (paramétrable). Une mauvaise réponse bloque l'app pendant 5 minutes. L'objectif est double : transformer les pulsions de procrastination en micro-sessions de révision, et apprendre Kotlin / Android natif sur un projet utile.

Pas de Play Store, pas de multi-utilisateurs, pas de cloud.

## 2. Objectifs d'apprentissage

- La structure d'un projet Android moderne (Gradle, modules, ressources)
- Kotlin
- Jetpack Compose pour l'UI
- Le cycle de vie Android (Activity, Service, foreground service)
- Les permissions
- La persistance locale
- La lecture d'assets JSON embarqués

## 3. Périmètre V1 (MVP)

### 3.1 Fonctionnalités incluses

**F1 — Sélection des apps surveillées**
Un écran qui liste les apps installées. L'utilisateur coche celles à surveiller. La sélection est persistée localement.

**F2 — Détection de lancement d'app surveillée**
Un service en arrière-plan détecte quand une app surveillée passe au premier plan.

**F3 — Affichage du quiz**
À la détection, l'app affiche un écran de quiz par-dessus (ou à la place de) l'app cible. L'utilisateur ne peut pas contourner l'écran sans répondre (à part en revenant au launcher).

**F4 — Logique du quiz**
Une question QCM tirée aléatoirement du pool. 4 réponses possibles, une seule correcte.
- Bonne réponse → l'app cible est accessible librement pour cette session
- Mauvaise réponse → l'app cible est bloquée pendant 5 minutes

**F5 — Blocage temporaire**
Pendant la fenêtre de blocage, toute tentative de réouverture de l'app cible réaffiche un écran de blocage avec un compte à rebours.

**F6 — Pool de questions**
Les questions sont dans un fichier JSON embarqué dans `assets/`. Format défini dans la section Architecture.

### 3.2 Hors périmètre V1

À garder en tête pour résister au scope creep. Ces fonctionnalités sont volontairement reportées :

- ❌ Éditeur de questions dans l'app
- ❌ Import depuis fichier externe (JSON/CSV)
- ❌ Statistiques de réussite, historique
- ❌ Système de répétition espacée
- ❌ Plusieurs niveaux de difficulté
- ❌ Durée de blocage configurable (fixe à 5 min)
- ❌ Plusieurs questions à la chaîne
- ❌ Catégories / thèmes au choix
- ❌ Mode strict anti-contournement (désinstaller l'app reste possible, c'est OK)
- ❌ Notifications, dark mode, animations sophistiquées
- ❌ Tests unitaires exhaustifs (quelques-uns sur la logique métier suffisent)

## 4. Contraintes techniques

| Élément | Choix |
|---|---|
| Langage | Kotlin |
| UI (framework) | Jetpack Compose |
| UI (design system) | Material 3 |
| Thème | Thème par défaut Material 3, dynamic color si Android 12+ |
| Min SDK | API 26 (Android 8.0) — couvre 95%+ des appareils et autorise les foreground services |
| Target SDK | API la plus récente disponible |
| Architecture | MVVM léger (ViewModel + State) |
| Persistance | DataStore Preferences |
| Détection d'app | `AccessibilityService` |
| Overlay | `Activity` translucide lancée par le service, plutôt que TYPE_APPLICATION_OVERLAY (plus simple) |
| Build | Gradle Kotlin DSL |

### Format JSON des questions

```json
[
  {
    "id": "q001",
    "theme": "algo",
    "question": "Quelle est la complexité moyenne d'une recherche dans une HashMap ?",
    "choices": ["O(1)", "O(log n)", "O(n)", "O(n log n)"],
    "correctIndex": 0
  }
]
```

## 5. Parcours utilisateur principal

1. Première ouverture → écran d'onboarding qui demande successivement :
    - Permission Accessibility (avec lien vers les paramètres système)
    - Permission Overlay (idem)
    - Désactivation de l'optimisation batterie (idem)
2. Écran de sélection des apps → cocher Twitter, Instagram, etc.
3. L'utilisateur ferme l'app de révisions et utilise son téléphone normalement
4. Il ouvre Twitter → écran de quiz s'affiche
5. Il répond → soit Twitter s'ouvre, soit retour au launcher avec compteur

## 6. Critères de succès

La V1 est terminée quand **toutes** ces conditions sont vraies :

- [ ] Je sélectionne au moins 2 apps à surveiller, la sélection persiste après reboot
- [ ] Ouvrir une app surveillée déclenche le quiz
- [ ] Une bonne réponse laisse passer pendant la session
- [ ] Une mauvaise réponse bloque pendant 5 min, vérifiable au chrono
- [ ] Le pool contient au moins 30 questions