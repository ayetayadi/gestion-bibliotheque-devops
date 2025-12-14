package com.bibliotheque.gestion_bibliotheque.entities.ressource;

public enum StatutRessource {
    DISPONIBLE,     // Ressource en rayon
    RESERVE,        // Déjà réservée par un lecteur
    INDISPONIBLE    // Cassée, perdue, ou temporairement non empruntable
}
