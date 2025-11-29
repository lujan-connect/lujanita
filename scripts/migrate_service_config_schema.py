import os
import logging

import firebase_admin
from firebase_admin import credentials
from google.cloud import firestore


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def init_firestore() -> firestore.Client:
    if os.getenv("FIRESTORE_EMULATOR_HOST"):
        logger.info("Using Firestore emulator at %s", os.getenv("FIRESTORE_EMULATOR_HOST"))
        project = os.getenv("GCP_PROJECT", "emulator")
        return firestore.Client(project=project)

    cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    cred = credentials.Certificate(cred_path) if cred_path else credentials.ApplicationDefault()
    if not firebase_admin._apps:
        firebase_admin.initialize_app(cred)
    return firestore.Client()


def migrate() -> None:
    db = init_firestore()
    services = db.collection("services").stream()
    migrated = 0

    for doc in services:
        data = doc.to_dict() or {}
        cfg = data.get("configSchema")
        if isinstance(cfg, dict):
            step = {
                "name": "Configuración",
                "instructions": None,
                "schema": cfg,
            }
            doc.reference.update({"configSchema": [step]})
            migrated += 1
            logger.info("Migrated service %s", doc.id)

    logger.info("Migration finished. %d documents updated", migrated)


if __name__ == "__main__":
    migrate()
