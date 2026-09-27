package com.lacasadelchef.erp.administracion.usuario;

import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoliticaUsuarioTest {

    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private PoliticaUsuario politica;

    private static Empleado empleado(String nombre, String apellido) {
        Empleado empleado = new Empleado();
        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        return empleado;
    }

    @Test
    @DisplayName("Primer nombre y primer apellido, en minusculas y sin tildes, igual que el correo")
    void nombrePuntoApellido() {
        assertThat(PoliticaUsuario.base("Ana Judith", "López Cáceres")).isEqualTo("ana.lopez");
        assertThat(PoliticaUsuario.base("Amado", "Soto Morales")).isEqualTo("amado.soto");
        assertThat(PoliticaUsuario.base("Rosario del Carmen", "Yax Poou")).isEqualTo("rosario.yax");
        assertThat(PoliticaUsuario.base("  Íñigo ", "Muñoz")).isEqualTo("inigo.munoz");
    }

    @Test
    @DisplayName("Si ya existe se agrega el primer numero libre: ana.lopez2, ana.lopez3...")
    void repetidoLlevaNumero() {
        when(usuarioRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(usuarioRepository.existsByUsernameIgnoreCase("ana.lopez")).thenReturn(true);
        when(usuarioRepository.existsByUsernameIgnoreCase("ana.lopez2")).thenReturn(true);

        assertThat(politica.generarPara(empleado("Ana Maria", "López Pérez"))).isEqualTo("ana.lopez3");
    }

    @Test
    @DisplayName("Si esta libre se usa tal cual")
    void libreSinNumero() {
        when(usuarioRepository.existsByUsernameIgnoreCase("amado.soto")).thenReturn(false);

        assertThat(politica.generarPara(empleado("Amado", "Soto Morales"))).isEqualTo("amado.soto");
    }
}
