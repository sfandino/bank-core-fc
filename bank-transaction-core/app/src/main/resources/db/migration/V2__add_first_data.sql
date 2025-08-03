-- Users
INSERT INTO users (id, name, email, created_at) VALUES
('7a1e2b10-1c2d-4e3f-8a9b-1c2d3e4f5a6b', 'Juan Dela Cruz', 'juan.delacruz@email.com', NOW()),
('8b2f3c21-2d3e-5f4a-9b8c-2d3e4f5a6b7c', 'Maria Santos', 'maria.santos@email.com', NOW()),
('9c3d4e32-3e4f-6a5b-8c9d-3e4f5a6b7c8d', 'John Smith', 'john.smith@email.com', NOW()),
('1d4e5f43-4f5a-7b6c-9d8e-4f5a6b7c8d9e', 'Emily Chan', 'emily.chan@email.com', NOW()),
('2e5f6a54-5a6b-8c7d-1e9f-5a6b7c8d9e1f', 'Liam Wong', 'liam.wong@email.com', NOW()),
('3f6a7b65-6b7c-9d8e-2f1a-6b7c8d9e1f2a', 'Olivia Garcia', 'olivia.garcia@email.com', NOW()),
('4a7b8c76-7c8d-1e9f-3a2b-7c8d9e1f2a3b', 'Noah Lee', 'noah.lee@email.com', NOW()),
('5b8c9d87-8d9e-2f1a-4b3c-8d9e1f2a3b4c', 'Sophia Kim', 'sophia.kim@email.com', NOW()),
('6c9d1e98-9e1f-3a2b-5c4d-9e1f2a3b4c5d', 'James Brown', 'james.brown@email.com', NOW()),
('7d1e2f09-1f2a-4b3c-6d5e-1f2a3b4c5d6e', 'Charlotte Cruz', 'charlotte.cruz@email.com', NOW()),
('8e2f3a10-2a3b-5c4d-7e6f-2a3b4c5d6e7f', 'Benjamin Tan', 'benjamin.tan@email.com', NOW()),
('9f3a4b21-3b4c-6d5e-8f7a-3b4c5d6e7f8a', 'Ava Lim', 'ava.lim@email.com', NOW());

-- Currencies
INSERT INTO currencies (code, name, symbol, created_at) VALUES
('PHP', 'Philippine Peso', '₱', NOW()),
('HKD', 'Hong Kong Dollar', 'HK$', NOW()),
('USD', 'US Dollar', '$', NOW()),
('EUR', 'Euro', '€', NOW()),
('GBP', 'British Pound', '£', NOW());