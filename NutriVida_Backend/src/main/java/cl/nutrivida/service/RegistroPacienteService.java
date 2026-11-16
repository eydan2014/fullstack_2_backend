package cl.nutrivida.service;

import cl.nutrivida.dto.PacienteRegistrado;
import cl.nutrivida.dto.RegistroPaciente;
import cl.nutrivida.dto.UsuarioRespuesta;
import org.springframework.stereotype.Service;

@Service
public class RegistroPacienteService {
    private final UsuarioService usuarios;

    public RegistroPacienteService(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    public PacienteRegistrado registrar(RegistroPaciente datos) {
        UsuarioRespuesta paciente = usuarios.registrar(datos);
        return new PacienteRegistrado(paciente.id(), paciente.nombre(), paciente.correo(), paciente.rol());
    }
}
