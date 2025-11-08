// Importa los módulos necesarios con la nueva sintaxis v2
import {onDocumentUpdated, onDocumentCreated} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

// Inicializa la conexión con Firebase
admin.initializeApp();
const db = admin.firestore();

/**
 * TRIGGER 1 (Sintaxis v2): Se activa cuando se actualiza un usuario.
 */
export const onUserVerified = onDocumentUpdated("users/{userId}", async (event) => {
  // El logger es la forma moderna de console.log en Cloud Functions
  logger.info(`Revisando actualización del usuario: ${event.params.userId}`);

  // Los datos antes y después del cambio ahora están en event.data
  const beforeData = event.data?.before.data();
  const afterData = event.data?.after.data();
  const userId = event.params.userId;

  // Si no hay datos o el campo no cambió, no hacemos nada
  if (!beforeData || !afterData || beforeData.fullyVerified === afterData.fullyVerified) {
    logger.info("El estado de verificación no cambió.");
    return;
  }

  let title = "";
  let body = "";

  if (afterData.fullyVerified === true) {
    title = "¡Cuenta Verificada!";
    body = "Felicidades, tu perfil ha sido verificado y ahora generas más confianza.";
  } else {
    title = "Verificación Rechazada";
    body = "No pudimos verificar tus datos. Por favor, revisa la información y vuelve a intentarlo.";
  }

  const notification = {
    userId: userId,
    title: title,
    body: body,
    type: "system_verification",
    isRead: false,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
  };

  logger.info(`Creando notificación de verificación para ${userId}`);
  await db.collection("user_notifications").add(notification);
});

/**
 * TRIGGER 2 (Sintaxis v2): Se activa cuando se crea un nuevo viaje.
 */
export const onTripBooked = onDocumentCreated("trips/{tripId}", async (event) => {
  logger.info(`Nuevo viaje creado: ${event.params.tripId}`);
  
  // El snapshot del documento creado ahora es event.data
  const tripData = event.data?.data();
  if (!tripData) {
    logger.error("No se encontraron datos en el nuevo viaje.");
    return;
  }
  
  const userId = tripData.passengerId;
  const date = tripData.date;
  const location = tripData.startLocation;

  const notification = {
    userId: userId,
    title: "¡Viaje Reservado!",
    body: `Tu viaje para el ${date} desde ${location} ha sido confirmado.`,
    type: "trip_booked",
    isRead: false,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
  };

  logger.info(`Creando notificación de reserva para ${userId}`);
  await db.collection("user_notifications").add(notification);
});