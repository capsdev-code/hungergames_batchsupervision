package com.capsdev.hungergames.domain;

/** Comportement du batch simulé lors d'un lancement. */
public enum Scenario {
    /** Déroulement normal, l'édition se termine. */
    NORMAL,
    /** Un district n'a aucun tribut : échec fonctionnel à la Moisson. */
    MISSING_DISTRICT,
    /** Timeout base de données en pleine Arène : échec technique. */
    TECHNICAL_FAILURE
}
