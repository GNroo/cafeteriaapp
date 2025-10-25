<?php
header("Content-Type: application/json");

$servername = "localhost";
$username = "root"; 
$password = ""; 
$dbname = "cafeteria"; 

// conexion
$conn = new mysqli($servername, $username, $password, $dbname);

// Chequear
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
?>