<?php
require 'db_connection.php';


$data = json_decode(file_get_contents('php://input'), true);


if (isset($data['id']) && isset($data['name']) && isset($data['price'])) {

    
    $id = (int)$data['id']; 
    $name = $conn->real_escape_string($data['name']);
    $description = isset($data['description']) ? $conn->real_escape_string($data['description']) : '';
    $price = (float)$data['price'];

    
    $sql = "UPDATE products SET name = '$name', description = '$description', price = $price WHERE id = $id";

    if ($conn->query($sql) === TRUE) {
        
        if ($conn->affected_rows > 0) {
            echo json_encode(['status' => 'success', 'message' => 'Producto actualizado correctamente']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'No se encontró el producto o los datos son los mismos']);
        }
    } else {
        
        echo json_encode(['status' => 'error', 'message' => 'Error al actualizar el producto: ' . $conn->error]);
    }

} else {
    
    echo json_encode(['status' => 'error', 'message' => 'Datos incompletos. Se requiere id, nombre y precio.']);
}

$conn->close();
?>