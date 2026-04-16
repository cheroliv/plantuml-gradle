# 🐛 Backlog — PlantUML Gradle Plugin

> **Objectif** : Suivi des issues et bugs en cours  
> **Session 75** : Correction test fonctionnel échoué (localisation FR)

---

## 🔴 Issues Critiques

### Issue #2 : Échec test `should handle read permission denied gracefully` (localisation FR)

**Statut** : 🔴 **À CORRIGER**  
**Priorité** : 🔴 **CRITIQUE** — **PRIORITÉ MAXIMALE**  
**Session** : 75  
**Impact** : Échec de `./gradlew koverVerify` et validation CI/CD

#### Symptômes
```
PlantumlFunctionalSuite > File permissions > should handle read permission denied gracefully() FAILED
    org.opentest4j.AssertionFailedError: Expected permission or access error but got:
    > Task :validatePlantumlSyntax FAILED
    java.io.FileNotFoundException: /tmp/junit-.../protected.puml (Permission non accordée)
```

#### Cause Racine
Le message d'erreur système est en **français** (`Permission non accordée`) mais l'assertion ne vérifie que les messages en **anglais** (`Permission denied`, `Access is denied`).

#### Fichier Concerné
- `src/functionalTest/kotlin/plantuml/PlantumlFunctionalSuite.kt:900-912`
- Méthode : `assertContainsPermissionOrNotFoundMessage`

#### Solution Requise
Ajouter les variantes de messages d'erreur en français :
- `Permission non accordée`
- `Accès refusé`
- `Impossible de lire`
- `Échec de la lecture`

#### Critère d'Acceptation
```bash
./gradlew -i koverVerify  # ✅ BUILD SUCCESSFUL
```

---

### Issue #1 : Crash système lors de l'exécution de `functionalTest`

**Statut** : 🟢 **RÉSOLU** (Session 74)  
**Priorité** : 🟡 **MOYENNE**  
**Session** : 73-74  
**Impact** : Historique — tests fonctionnels maintenant stables

#### Résumé
Problème de mémoire et de stabilité résolu en Session 74 par :
- Augmentation mémoire Gradle
- Correction des mocks et de la réflexion
- Migration mockito-kotlin

**Résultats Session 74** :
- ✅ 38 tests fonctionnels PASS, 7 SKIP, 0 FAIL
- ✅ 203 tests unitaires PASS (100%)

#### Symptômes
- La tâche `functionalTest` fait crasher le système
- Possible OutOfMemoryError ou freeze complet

#### Hypothèses
1. **Mémoire insuffisante** : `org.gradle.jvmargs=-Xmx512m` (trop bas pour 51 tests)
2. **Test Kit** : Gradle TestKit crée des processus multiples
3. **Fuite mémoire** : Un test ne libère pas les ressources

#### Pistes de Debug
- ✅ Vérifier les rapports de tests existants (déjà générés)
- ⏳ Augmenter la mémoire Gradle : `-Xmx2g`
- ⏳ Exécuter avec `--debug` pour voir les logs détaillés
- ⏳ Identifier quel test fait crasher (exécution par sous-ensemble)
- ⏳ Vérifier les logs système (dmesg, /var/log/syslog)

#### Configuration Actuelle
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+UseSerialGC
```

#### Configuration Recommandée (pour debug)
```properties
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC
```

---

## 📊 État des Tests (Dernière Exécution)

**Source** : `build/reports/tests/functionalTest/` (15 avr. 2026, 22:51)

| Suite | Tests | Failures | Skipped | Duration | Success Rate |
|-------|-------|----------|---------|----------|--------------|
| **Total** | **51** | **0** | **11** | **56.733s** | **100%** |
| PluginLifecycle | 6 | 0 | 0 | 29.918s | 100% |
| LlmProviderConfiguration | 8 | 0 | 6 | 2.189s | 100% |
| GradleSharedInstance | 4 | 0 | 0 | 0.600s | 100% |
| PluginIntegration | 11 | 0 | 0 | 7.845s | 100% |
| FilePermission | 4 | 0 | 0 | 0.667s | 100% |
| LargeFileAndPath | 4 | 0 | 0 | 1.381s | 100% |
| NetworkTimeout | 4 | 0 | 0 | 3.703s | 100% |
| Performance | 4 | 0 | 0 | 0.723s | 100% |
| RagTask | 6 | 0 | 5 | 0.366s | 100% |

**Conclusion** : Les rapports montrent 100% de succès, mais le crash survient peut-être :
- Après l'exécution des tests (nettoyage, fermeture)
- Lors d'une exécution ultérieure (cache, état résiduel)
- Dans un environnement différent (CI vs local)

---

## 📋 Plan d'Action — Session 73

### Phase 1 : Investigation (NE PAS lancer functionalTest)
- [x] Analyser les rapports de tests existants
- [x] Vérifier configuration mémoire Gradle
- [ ] Examiner logs système (si disponibles)
- [ ] Identifier le point de crash exact

### Phase 2 : Correctifs Potentiels
- [ ] Augmenter mémoire Gradle (`-Xmx2g`)
- [ ] Changer GC (`-XX:+UseG1GC` au lieu de `SerialGC`)
- [ ] Exécuter tests par sous-ensembles isolés
- [ ] Désactiver Gradle Daemon pour les tests

### Phase 3 : Validation
- [ ] Exécuter `functionalTest --tests "*quick*"` (18 tests rapides)
- [ ] Si OK, exécuter `functionalTest --tests "*slow*"` (18 tests lents)
- [ ] Si OK, exécuter tous les tests
- [ ] Surveiller consommation mémoire (htop, jvisualvm)

---

## ⚠️ Commandes à Risque (À ÉVITER pour l'instant)

```bash
# ❌ NE PAS EXÉCUTER (crash système)
./gradlew functionalTest

# ✅ SÛR (tests unitaires uniquement)
./gradlew test

# ✅ SÛR (tests fonctionnels rapides uniquement)
./gradlew functionalTest --tests "*quick*"
```

---

## 📝 Notes d'Investigation

### Session 73 — Démarrage
- **Date** : 15 avr. 2026
- **Objectif** : Debug crash functionalTest
- **Première constatation** : Rapports de tests montrent 100% de succès
- **Hypothèse principale** : Crash post-exécution ou mémoire insuffisante

### Actions à Prioriser
1. **NE PAS lancer `functionalTest`** tant que la cause n'est pas identifiée
2. **Augmenter mémoire** dans `gradle.properties`
3. **Tester progressivement** (quick → slow → tous)

---

## 🔗 Liens Utiles

- **Rapports de tests** : `build/reports/tests/functionalTest/`
- **Résultats XML** : `build/test-results/functionalTest/`
- **Logs Gradle** : `~/.gradle/daemon/*/daemon-*.log`
- **Logs système** : `dmesg`, `/var/log/syslog`

---

**Dernière mise à jour** : Session 73 (15 avr. 2026)
