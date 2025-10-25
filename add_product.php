<?php
require 'db_connection.php';


$data = json_decode(file_get_contents('php://input'), true);


if (isset($data['name']) && isset($data['price'])) {

   
    $name = $conn->real_escape_string($data['name']);
    $description = isset($data['description']) ? $conn->real_escape_string($data['description']) : '';
    $price = (float)$data['price'];

   
    $sql = "INSERT INTO products (name, description, price) VALUES ('$name', '$description', $price)";

    if ($conn->query($sql) === TRUE) {
        
        echo json_encode(['status' => 'success', 'message' => 'Producto agregado correctamente']);
    } else {
        
        echo json_encode(['status' => 'error', 'message' => 'Error al agregar el producto: ' . $conn->error]);
    }

} else {
    
    echo json_encode(['status' => 'error', 'message' => 'Datos incompletos. Se requiere nombre y precio.']);
}

$conn->close();
?>