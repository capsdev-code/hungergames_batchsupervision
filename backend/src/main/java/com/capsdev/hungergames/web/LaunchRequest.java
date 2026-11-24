package com.capsdev.hungergames.web;

import com.capsdev.hungergames.domain.Scenario;

/**
 * Corps du lancement ou de la relance d'une édition. Tout est optionnel :
 * scénario par défaut {@code NORMAL}, district manquant par défaut 2.
 */
public record LaunchRequest(Scenario scenario, Integer missingDistrict) {
}
