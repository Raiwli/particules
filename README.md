# Particules

Application JavaFX plein écran de 150 particules colorées qui rebondissent sur les bords et lors de collisions élastiques.

## Classes

- `Particle` représente une particule et applique le déplacement, les rebonds sur les bords et la résolution d'une collision élastique.
- `HeavyParticle` est une particule dont la masse vaut dix masses standard.
- `GhostParticle` traverse les autres particules sans interaction.
- `StickyParticle` absorbe les particules qu'elle touche.
- `ParticleSystem` crée les particules sans chevauchement, répartit les collisions dans une grille et fait avancer le modèle par petits pas de temps.
- `ParticleApplication` construit la fenêtre JavaFX, lit les touches et dessine l'état du modèle.

## Choix de conception

Le modèle (`model`) ne dépend pas de JavaFX : ses particules sont des objets Java qui exposent seulement leurs valeurs d'affichage, tandis que le système garde la collection et pilote les mises à jour. Les types spécialisés redéfinissent leurs propriétés de collision sans test de type. Une grille spatiale limite les comparaisons aux cellules voisines. La vue (`view`) traduit les valeurs en formes JavaFX et conserve le compteur de collisions; le modèle retourne le nombre d'événements de collision de chaque pas. La résolution élastique utilise les masses de chaque particule et conserve l'énergie cinétique. Le pas de temps est découpé pour limiter les traversées entre deux images.

Java 21 et Maven sont requis. JavaFX est téléchargé automatiquement par Maven.

```sh
mvn javafx:run
```

Appuyez sur Espace pour mettre en pause ou reprendre, et sur Échap pour quitter.
