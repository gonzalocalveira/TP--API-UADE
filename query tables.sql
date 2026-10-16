CREATE TABLE Asiento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_asiento INT NOT NULL,
    fila_asiento VARCHAR(255) NOT NULL,
    sala_id INT NOT NULL,

#CONSTRAINT fk_asiento_sala
        #FOREIGN KEY (sala_id)
        #REFERENCES sala(id),
    
    
    
    CONSTRAINT chk_numero_asiento
        CHECK (numero_asiento > 0)
	);