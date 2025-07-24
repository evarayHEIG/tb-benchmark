# tb-benchmark

Ce repository contient le code source et la documentation de mon projet de bachelor 

## Résumé

Avec l'essor des applications manipulant de grands volumes de données semi-structurées, les bases de données NoSQL orientées documents sont devenues une alternative populaire aux modèles relationnels traditionnels. Toutefois, des SGBD relationnels matures comme PostgreSQL intègrent désormais des fonctionnalités natives pour gérer des données documentaires, notamment via le type JSONB, brouillant les frontières entre les deux paradigmes.

Dans ce contexte, une question centrale émerge : comment les choix de modélisation des données influencent-ils concrètement la performance de la recherche dans des collections de documents?

L'objectif principal de ce travail était de concevoir et développer une plateforme de benchmark pour comparer l'impact de trois modèles de stockage sur la performance de la recherche documentaire : PostgreSQL classique (mode relationnel), PostgreSQL avec le type JSONB (mode document), et Couchbase (mode document natif). Développée en Java et Vue.js, et déployée via Docker, la plateforme permet de configurer et lancer des benchmarks de manière automatisée.

L’évaluation a été menée en exécutant des workloads de requêtes représentatives (filtrage, jointures...) dans un environnement contrôlé. Des métriques comme la latence ou l'efficacité du cache ont été mesurées pour assurer une comparaison objective.

Les résultats révèlent une hiérarchie de performance claire : le modèle relationnel de PostgreSQL reste le plus performant dans la majorité des cas. Cependant, l'approche hybride de PostgreSQL avec son type JSONB s'impose comme une alternative très compétitive, surpassant significativement les performances de Couchbase.

Ces conclusions positionnent le type JSONB de PostgreSQL comme une solution sérieuse et performante pour la gestion de données documentaires, alliant la souplesse NoSQL à la maturité de PostgreSQL. La plateforme développée constitue une base solide pour de futures études sur d’autres modèles avec de nouveaux paramètres.

## Contenu du repository

Chaque dossier du repository contient son propre fichier README.md avec des instructions spécifiques, si c'est pertinent. Voici un aperçu des dossiers et de leur contenu :

- `docker/`: Contient les fichiers nécessaires pour déployer l'application avec Docker.
- `backend/`: Contient le code source du backend de l'application, développé en Java.
- `frontend/`: Contient le code source du frontend de l'application, développé en Vue.js.
- `configuration/`: Contient une explication détaillée du format des fichiers de configuration utilisés pour les benchmarks, ainsi que des exemples de fichiers de configurations.
- `doc/`: Contient la documentation du projet
  - le rapport final du travail de bachelor
  - l'affiche de présentation du projet
- `annexe/`: Contient des documents supplémentaires pour le projet
  - Les rapports de benchmark complet présentés dans le rapport final.
  - Un UML complet du diagramme de classes du jeux de données `Yelp Open Dataset` utilisé pour modéliser la base de données relationnelle.
