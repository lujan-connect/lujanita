from flask import Flask, request, jsonify
import json

app = Flask(__name__)

@app.route('/mcp', methods=['POST'])
def mcp():
    req = request.get_json()
    method = req.get('method')
    params = req.get('params', {})

    # Simular respuestas según el método MCP
    if method == 'orders.get':
        order_id = params.get('orderId', 'SO001')
        response = {
            "orderId": order_id,
            "status": "confirmed",
            "customerName": "Juan Perez",
            "totalAmount": 123.45,
            "createdAt": "2025-11-29T10:00:00Z"
        }
        if params.get('includeLines'):
            response["lines"] = [
                {"productId": "P001", "productName": "Producto 1", "quantity": 2, "price": 50.0}
            ]
    elif method == 'customers.search':
        response = {
            "customers": [{"customerId": "C001", "customerName": "Juan Perez"}],
            "totalCount": 1,
            "limit": params.get('limit', 20),
            "offset": params.get('offset', 0)
        }
    elif method == 'products.search':
        response = {
            "products": [{"productId": "P001", "productName": "Producto 1", "price": 100.0}],
            "totalCount": 1
        }
    elif method == 'orders.list':
        response = {
            "orders": [{"orderId": "SO001", "status": "confirmed"}],
            "totalCount": 1,
            "limit": params.get('limit', 20),
            "offset": params.get('offset', 0)
        }
    elif method == 'customers.get':
        response = {
            "customerId": "C001",
            "customerName": "Juan Perez",
            "email": "juan@example.com",
            "phone": "+54912345678"
        }
    else:
        # Método desconocido
        response = {"ok": True, "message": f"Método {method} simulado"}

    return jsonify(response)

if __name__ == '__main__':
    app.run(port=8069, debug=True)
