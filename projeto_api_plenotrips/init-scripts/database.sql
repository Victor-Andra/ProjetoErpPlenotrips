-- Habilita a extensão para geração de UUIDs, caso ainda não esteja habilitada.
-- Em alguns ambientes de banco de dados como serviço (ex: AWS RDS),
-- esta extensão pode precisar ser habilitada por um superusuário.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Remove as tabelas existentes na ordem correta para evitar erros de chave estrangeira.
-- A cláusula CASCADE remove automaticamente as dependências (como as chaves estrangeiras).
DROP TABLE IF EXISTS delivery_invoices;
DROP TABLE IF EXISTS delivery_assignments;
DROP TABLE IF EXISTS drivers;
DROP TABLE IF EXISTS vehicles;

-- Tabela: drivers
-- Armazena os dados dos motoristas e seu status atual.
CREATE TABLE drivers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'IN_TRANSIT', 'OFF_DUTY')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Tabela: vehicles
-- Armazena os dados dos veículos e seu status operacional.
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plate_number VARCHAR(10) NOT NULL UNIQUE,
    model VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPERATIONAL', 'MAINTENANCE', 'IN_TRANSIT')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Tabela: delivery_assignments
-- Tabela de junção que armazena as designações de entrega. Será populada pela aplicação Go.
CREATE TABLE delivery_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    route_description TEXT NOT NULL,
    driver_id UUID NOT NULL,
    vehicle_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT fk_driver
        FOREIGN KEY(driver_id) 
        REFERENCES drivers(id),
    
    CONSTRAINT fk_vehicle
        FOREIGN KEY(vehicle_id) 
        REFERENCES vehicles(id)
);

-- Tabela: delivery_invoices
-- Armazena as notas fiscais associadas a cada entrega.
CREATE TABLE delivery_invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_assignment_id UUID NOT NULL,
    invoice_number VARCHAR(255) NOT NULL,
    -- JSONB é ideal para armazenar dados semiestruturados como detalhes da nota fiscal.
    invoice_details JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),

    -- Garante que se uma entrega for deletada, suas notas também serão.
    CONSTRAINT fk_delivery_assignment 
        FOREIGN KEY(delivery_assignment_id) 
        REFERENCES delivery_assignments(id)
        ON DELETE CASCADE
);

-- Adiciona comentários para maior clareza no schema.
COMMENT ON TABLE drivers IS 'Contém informações sobre os motoristas da frota.';
COMMENT ON COLUMN drivers.status IS 'Status atual do motorista: AVAILABLE, IN_TRANSIT, OFF_DUTY.';
COMMENT ON TABLE vehicles IS 'Contém informações sobre os veículos da frota.';
COMMENT ON COLUMN vehicles.status IS 'Status operacional do veículo: OPERATIONAL, MAINTENANCE.';
COMMENT ON TABLE delivery_assignments IS 'Registra a atribuição de uma entrega a um motorista e veículo.';
COMMENT ON TABLE delivery_invoices IS 'Armazena as notas fiscais associadas a cada designação de entrega (delivery_assignments).';

---------------------------------------------------------------------------------
-- INÍCIO DA MASSA DE TESTE
---------------------------------------------------------------------------------

-- Inserção de Motoristas com timestamps distintos
INSERT INTO drivers (name, license_number, status, created_at) VALUES
('Carlos Silva', '123456789', 'AVAILABLE', '2025-09-30 10:00:00-03'), -- O motorista disponível MAIS ANTIGO. DEVE SER O ESCOLHIDO.
('Mariana Costa', '987654321', 'AVAILABLE', '2025-09-30 11:00:00-03'), -- O segundo motorista disponível mais antigo.
('Ricardo Souza', '112233445', 'IN_TRANSIT', '2025-09-30 12:00:00-03'), -- Ocupado, deve ser ignorado pela lógica.
('Juliana Pereira', '556677889', 'OFF_DUTY', '2025-09-30 13:00:00-03');  -- De folga, deve ser ignorado pela lógica.

-- Inserção de Veículos com timestamps distintos
INSERT INTO vehicles (plate_number, model, status, created_at) VALUES
('BRA2E19', 'Scania R450', 'OPERATIONAL', '2025-09-30 09:00:00-03'), -- O veículo operacional MAIS ANTIGO. DEVE SER O ESCOLHIDO.
('RIO2A18', 'Volvo FH 540', 'OPERATIONAL', '2025-09-30 14:00:00-03'), -- O segundo veículo operacional mais antigo.
('SAO1B27', 'Mercedes-Benz Actros', 'MAINTENANCE', '2025-09-30 15:00:00-03'); -- Em manutenção, deve ser ignorado.

-- A tabela `delivery_assignments` começa vazia, pois será populada pela aplicação Go durante o teste.

---------------------------------------------------------------------------------
-- FIM DA MASSA DE TESTE
---------------------------------------------------------------------------------