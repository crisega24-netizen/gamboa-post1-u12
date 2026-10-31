package com.empresa.reembolsos.dominio;

/** Estados de una solicitud; cada cambio de estado se notifica a los interesados (RF3). */
public enum EstadoSolicitud { PENDIENTE, APROBADA, RECHAZADA, REQUIERE_CORRECCION }
