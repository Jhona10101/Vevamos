import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// Inicializa la conexión con Firebase
admin.initializeApp();
const db = admin.firestore();

/**
 * TRIGGER 1: Se activa cuando se actualiza un usuario.
 * Vigila el cambio en el campo 'fullyVerified'.
 */
export const onUserVerified = functions.firestore
  .document("users/{userId}")
  .onUpdate(async (change) => {
    const beforeData = change.before.data();
    const afterData = change.after.data();
    const userId = change.after.id;

    // Solo continuamos si 'fullyVerified' cambió
    if (beforeData.fullyVerified === afterData.fullyVerified) {
      return null;
    }

    let title = "";
    let body = "";

    if (afterData.fullyVerified === true) {
      title = "¡Cuenta Verificada!";
      // --- CORRECCIÓN 1: Se partió la línea larga ---
      body = "Felicidades, tu perfil ha sido verificado y ahora generas " +
             "más confianza.";
    } else {
      title = "Verificación Rechazada";
      // --- CORRECCIÓN 2: Se partió la línea larga ---
      body = "No pudimos verificar tus datos. Por favor, revisa la " +
             "información y vuelve a intentarlo.";
    }

    const notification = {
      userId: userId,
      title: title,
      body: body,
      type: "system_verification",
      isRead: false,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    };

    return db.collection("user_notifications").add(notification);
  });

/**
 * TRIGGER 2: Se activa cuando se crea un nuevo viaje.
 * Asumo que tienes una colección 'trips' y guardas el 'passengerId'.
 */
export const onTripBooked = functions.firestore
  .document("trips/{tripId}")
  .onCreate(async (snapshot) => {
    const tripData = snapshot.data();
    // Asegúrate de que estos campos existan en tus documentos de viajes
    const userId = tripData.passengerId;
    const date = tripData.date; // ej: "10/11/2025"
    const location = tripData.startLocation; // ej: "Plaza Mayor"

    const notification = {
      userId: userId,
      title: "¡Viaje Reservado!",
      body: `Tu viaje para el ${date} desde ${location} ha sido confirmado.`,
      type: "trip_booked",
      isRead: false,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    };

    return db.collection("user_notifications").add(notification);
  });

// --- CORRECCIÓN 3: Se añadió una línea en blanco al final del archivo ---