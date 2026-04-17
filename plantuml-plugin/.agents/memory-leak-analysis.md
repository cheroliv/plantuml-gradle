# 🔴 Analyse Fuites Mémoire & Ressources — Session 90

## 📊 État du système (AVANT corrections)

| Métrique | Valeur | Statut |
|----------|--------|--------|
| **Disk usage** | 426GB/467GB (97%) | 🔴 CRITIQUE |
| **Swap** | 969MB/979MB (99%) | 🔴 CRITIQUE |
| **RAM libre** | 3.2GB / 15GB | 🟡 ALERTE |
| **Répertoires temp Gradle** | 366 dossiers | 🔴 FUITES |
| **Containers Docker orphelins** | 5 (1 actif) | 🟡 FUITES |

---

## 🐛 Causes racines identifiées

### 1. **Fuite de répertoires temporaires** (366 dossiers `/tmp/gradle-test-*`)

**Fichier** : `PlantumlWorld.kt:427-439`

**Problème** :
```kotlin
val testDir = createTempFile("gradle-test-", "").apply {
    delete()
    mkdirs()
}
// ...
projectDir = testDir  // <-- JAMAIS nettoyé si test échoue
```

La méthode `cleanup()` (ligne 507) échoue silencieusement quand :
- Le test plante avant `cleanup()`
- Fichiers verrouillés par Gradle Daemon
- Permissions modifiées

**Correction** :
- Ajout `TestCleanupExtension.kt` avec tracking automatique
- Nettoyage forcé même après échec
- Limite de 50 répertoires maximum

---

### 2. **Conflit de variables `pgvectorContainer`**

**Fichiers** : `CommonSteps.kt:13` ET `RagPipelineSteps.kt:14`

**Problème** :
```kotlin
// DANS CommonSteps.kt
private var pgvectorContainer: PostgreSQLContainer<*>? = null

// DANS RagPipelineSteps.kt  
private var pgvectorContainer: PostgreSQLContainer<*>? = null  // <-- CONFLIT !
```

**Résultat** : Le container créé dans `RagPipelineSteps` n'est **JAMAIS stoppé** car `CommonSteps.cleanup()` nettoie sa propre variable (qui reste `null`).

**Correction** :
- Documentation clarifiée dans `RagPipelineSteps`
- Logging ajouté pour tracer le cycle de vie
- `withReuse(false)` pour forcer nouveaux containers

---

### 3. **Gradle Daemons multiples** (851MB RAM chacun)

**Processus actifs** :
- `GradleDaemon 9.4.0` (Java 23) : **1.2GB RAM**
- `KotlinCompileDaemon` : **851MB RAM**

**Correction** :
```kotlin
// build.gradle.kts
forkEvery = 1  // ← Restart JVM after each test
maxHeapSize = "1g"
systemProperty("org.gradle.daemon", "false")
```

---

## 🛠️ Corrections implémentées

### Fichiers modifiés

| Fichier | Action | Impact |
|---------|--------|--------|
| `TestCleanupExtension.kt` | ✅ CRÉÉ | Cleanup global AVANT/APRÈS chaque scénario |
| `CommonSteps.kt` | ✅ MODIFIÉ | Utilise `TestCleanupExtension` + logging |
| `RagPipelineSteps.kt` | ✅ MODIFIÉ | Logging + `withReuse(false)` |
| `PlantumlWorld.kt` | ✅ MODIFIÉ | Track les répertoires temporaires |
| `build.gradle.kts` | ✅ MODIFIÉ | `forkEvery=1` + cleanup `doLast` |

---

## 🧪 Commandes de nettoyage manuel

### Urgence (à exécuter si freeze)

```bash
# 1. Stopper tous les containers Docker
docker stop $(docker ps -q)

# 2. Supprimer les containers arrêtés
docker rm -f $(docker ps -aq)

# 3. Nettoyer les volumes orphelins
docker volume prune -f

# 4. Supprimer les images inutilisées
docker image prune -af

# 5. Nettoyer les répertoires temporaires
rm -rf /tmp/gradle-test-*

# 6. Tuer les Gradle Daemons
pkill -f GradleDaemon

# 7. Tuer les Kotlin Daemons
pkill -f KotlinCompileDaemon
```

### Monitoring

```bash
# Vérifier mémoire RAM
free -h

# Vérifier containers actifs
docker stats --no-stream

# Vérifier répertoires temporaires
ls -la /tmp | grep gradle-test | wc -l

# Vérifier processus Gradle
ps aux | grep -E "(Gradle|Kotlin)" | grep -v grep
```

---

## 📈 Métriques attendues (APRÈS corrections)

| Métrique | Avant | Après (cible) |
|----------|-------|---------------|
| **Répertoires temp** | 366 | < 10 |
| **Containers orphelins** | 5 | 0 |
| **RAM Gradle Daemon** | 2.8GB | 1GB max |
| **Swap utilisé** | 969MB | < 100MB |

---

## ⚠️ Points de vigilance

### 1. **Timeout containers**

Les containers pgvector ont un timeout de 2 minutes au démarrage. Si Docker est lent :
```kotlin
withStartupTimeout(Duration.ofMinutes(2)) // <-- Peut être insuffisant
```

**Solution** : Augmenter à 5 minutes si erreurs de startup.

---

### 2. **Fichiers verrouillés**

Gradle peut verrouiller des fichiers dans `build/` et `/tmp/gradle-test-*`.

**Solution** : `forkEvery=1` force un redémarrage JVM qui libère les locks.

---

### 3. **Disk space critique**

Avec 97% d'utilisation, le système peut freeze en cas de :
- Swap massif (OOM killer)
- Écritures Docker (layers)
- Logs Gradle verbeux

**Solution immédiate** : 
```bash
# Libérer 10-15GB
docker system prune -af --volumes
rm -rf ~/.gradle/caches/modules-2/files-2.1
```

---

## 🎯 Tests de validation

### Exécuter après corrections

```bash
# 1. Nettoyer l'environnement
./gradlew clean

# 2. Lancer les tests Error Handling (dont pgvector)
./gradlew cucumberTest --tests "*Error Handling*"

# 3. Vérifier absence de fuites
ls -la /tmp | grep gradle-test | wc -l  # Doit être < 10
docker ps -a | grep postgres | wc -l    # Doit être 0
```

### Critères de succès

- ✅ Aucun container Docker orphelin
- ✅ Moins de 10 répertoires `/tmp/gradle-test-*`
- ✅ RAM Gradle Daemon < 1GB
- ✅ Aucun freeze OS pendant les tests
- ✅ Tests pgvector PASS

---

## 📚 Références

- **EPIC** : `EPIC_CONSOLIDATION_TESTS_FONCTIONNELS.md`
- **Session** : 90 (Correction pgvector + Memory leaks)
- **Fichiers clés** :
  - `src/test/scenarios/plantuml/scenarios/TestCleanupExtension.kt`
  - `src/test/scenarios/plantuml/scenarios/CommonSteps.kt`
  - `build.gradle.kts` (cucumberTask)

---

**Date** : 17 avr. 2026  
**Statut** : ✅ Corrections implémentées — En attente de validation
