<?php
require 'db_connection.php';


$data = json_decode(file_get_contents('php://input'), true);


if (isset($data['id'])) {

    $id = (int)$data['id']; 

    
    
    $sql_history = "DELETE FROM purchase_history WHERE product_id = $id";
    $conn->query($sql_history); 

    
    $sql_product = "DELETE FROM products WHERE id = $id";

    if ($conn->query($sql_product) === TRUE) {
        
        if ($conn->affected_rows > 0) {
            echo json_encode(['status' => 'success', 'message' => 'Producto eliminado correctamente']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'No se encontró el producto para eliminar']);
        }
    } else {
        
        echo json_encode(['status' => 'error', 'message' => 'Error al eliminar el producto: ' . $conn->error]);
    }

} else {
    
    echo json_encode(['status' => 'error', 'message' => 'No se proporcionó el ID del producto.']);
}

$conn->close();
?>