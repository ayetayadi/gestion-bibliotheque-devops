package com.bibliotheque.gestion_bibliotheque.metier;

import com.bibliotheque.gestion_bibliotheque.dao.BibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.dao.StockBibliothequeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BibliothequeServiceTest {

    @Mock
    private BibliothequeRepository bibliothequeRepository;

    @Mock
    private UtilisateurService utilisateurService;

    @Mock
    private PretRepository pretRepository;

    @Mock
    private StockBibliothequeRepository stockRepository;

    @InjectMocks
    private BibliothequeService bibliothequeService;

    @Test
    void shouldReturnNumberOfActiveBibliotheques() {
        // GIVEN
        when(bibliothequeRepository.countByActifTrue()).thenReturn(4L);

        // WHEN
        long result = bibliothequeService.nombreBibliothequesActives();

        // THEN
        assertEquals(4L, result);
    }

    @Test
    void shouldReturnNumberOfInactiveBibliotheques() {
        when(bibliothequeRepository.countByActifFalse()).thenReturn(2L);

        long result = bibliothequeService.nombreBibliothequesInactives();

        assertEquals(2L, result);
    }
}
