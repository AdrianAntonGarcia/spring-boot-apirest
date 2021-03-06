package com.bolsaideas.springboot.backend.apirest.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bolsaideas.springboot.backend.apirest.models.entity.Cliente;
import com.bolsaideas.springboot.backend.apirest.models.services.IClienteService;

@CrossOrigin(origins = { "http://localhost:4200" }, methods = { RequestMethod.GET, RequestMethod.POST,
		RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping("/api")
public class ClienteRestController {

	@Autowired
	private IClienteService clienteService;

	@GetMapping("/clientes")
	public List<Cliente> index() {
		return clienteService.findAll();
	}

	@GetMapping("/clientes/{id}")
	public ResponseEntity<?> show(@PathVariable Long id) {
		Cliente cliente = null;
		Map<String, Object> response = new HashMap<>();
		try {
			cliente = clienteService.findById(id);
		} catch (DataAccessException e) {
			response.put("error", "Error en la base de datos");
			response.put("mensaje", e.getMostSpecificCause().getMessage());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (cliente == null) {
			response.put("mensaje", "El cliente ID: ".concat(id.toString().concat(" no existe en la base de datos.")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Cliente>(cliente, HttpStatus.OK);
	}

	@PostMapping("/clientes")
	@ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<?> create(@Valid @RequestBody Cliente cliente, BindingResult result) {
		Map<String, Object> response = new HashMap<>();

		ResponseEntity<?> resp = validErrors(result).orElse(null);
		if (resp != null) {
			return resp;
		}

//		if (result.hasErrors()) {
//			List<String> errors = new ArrayList<>();
//			for (FieldError err : result.getFieldErrors()) {
//				errors.add("El campo '" + err.getField() + "' " + err.getDefaultMessage());
//			}
//			List<String> errors = result.getFieldErrors().stream()
//					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
//					.collect(Collectors.toList());
//			response.put("errors", errors);
//			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
//		}
		Cliente newCliente = null;

		try {
			newCliente = clienteService.save(cliente);
		} catch (ConstraintViolationException e) {
			response.put("error", "Error en la base de datos");
			List<String> listaErrores = new ArrayList<String>();
			for (ConstraintViolation<?> constraint : e.getConstraintViolations()) {
				listaErrores.add(constraint.getMessage());
			}
			response.put("mensaje", listaErrores.toString());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		} catch (DataAccessException e) {
			response.put("error", "Error en la base de datos");
			response.put("mensaje", e.getMostSpecificCause().getMessage());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("mensaje", "El cliente ha sido creado con ??xito");
		response.put("cliente", newCliente);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	@PutMapping("/clientes/{id}")
//	@ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<?> update(@Valid @RequestBody Cliente cliente, BindingResult result, @PathVariable Long id) {
		Map<String, Object> response = new HashMap<>();
		ResponseEntity<?> resp = validErrors(result).orElse(null);
		if (resp != null)
			return resp;
		Cliente clienteActual = clienteService.findById(id);
		if (clienteActual == null) {
			response.put("mensaje", "Error: no se pudo editar, el cliente ID: "
					.concat(id.toString().concat(" no existe en la base de datos.")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		Cliente clienteUpdated = null;
		try {
			if (cliente.getApellido() != null)
				clienteActual.setApellido(cliente.getApellido());
			if (cliente.getNombre() != null)
				clienteActual.setNombre(cliente.getNombre());
			if (cliente.getEmail() != null)
				clienteActual.setEmail(cliente.getEmail());
			clienteUpdated = clienteService.save(clienteActual);
		} catch (DataAccessException e) {
			response.put("error", "Error en la base de datos");
			response.put("mensaje", e.getMostSpecificCause().getMessage());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("mensaje", "El cliente ha sido actualizado con ??xito");
		response.put("cliente", clienteUpdated);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	@DeleteMapping("/clientes/{id}")
//	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public ResponseEntity<?> delete(@PathVariable Long id) {
		Map<String, Object> response = new HashMap<>();
		try {
			clienteService.delete(id);
			response.put("mensaje", "El cliente ha sido eliminado con ??xito!");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (DataAccessException e) {
			response.put("error", "Error en la base de datos");
			response.put("mensaje", e.getMostSpecificCause().getMessage());
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Optional<ResponseEntity<?>> validErrors(BindingResult result) {
		Map<String, Object> response = new HashMap<>();
		if (result.hasErrors()) {
//			List<String> errors = new ArrayList<>();
//			for (FieldError err : result.getFieldErrors()) {
//				errors.add("El campo '" + err.getField() + "' " + err.getDefaultMessage());
//			}
			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());
			response.put("errors", errors);
			return Optional.of(new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST));
		}
		return Optional.empty();
	}

}
