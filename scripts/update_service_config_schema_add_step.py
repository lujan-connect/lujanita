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


def update(serviceId: str, index: int, value) -> None:
    db = init_firestore()
    service = db.collection("services").document(serviceId).get()
    service_data = service.to_dict()
    service_data["configSchema"].insert(index, value)

    service.reference.update({"configSchema": service_data.get("configSchema")})

    logger.info("Update finished. Service %s updated\nNew configSchema: %s", serviceId, service_data.get("configSchema"))


if __name__ == "__main__":
    update(
        "2zVKJCijItxAiSE24yFe",
        1,
        {
            'name': 'DESTINATIONS',
            'stepSchema': {
                'destinations': {
                    'arrayType': 'string',
                    'type': 'array',
                    'required': True
                }
            },
            'instructions': 'SELECT_DESTINATIONS',
        },
    )
