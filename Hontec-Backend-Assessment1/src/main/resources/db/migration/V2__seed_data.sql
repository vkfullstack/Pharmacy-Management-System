-- Passwords are "password" hashed with BCrypt
INSERT INTO users (email, password, role) VALUES 
('admin@pharmacy.com', '$2a$10$wM0x41rFqR2k7hM80aM2.O0GqF2p53C9G.7x/X.gL951eK2eS5kU2', 'ADMIN'),
('pharmacist@pharmacy.com', '$2a$10$wM0x41rFqR2k7hM80aM2.O0GqF2p53C9G.7x/X.gL951eK2eS5kU2', 'PHARMACIST');

INSERT INTO customers (name, phone) VALUES
('John Doe', '1234567890'),
('Jane Smith', '0987654321');

INSERT INTO doctors (name, specialization) VALUES
('Dr. Gregory House', 'Diagnostics'),
('Dr. John Watson', 'General Medicine');

-- Seed Products
-- Amoxicillin (not low stock)
INSERT INTO products (id, name, category, hsn_code, stock_qty, low_stock_threshold, expiry_date, created_at) VALUES
(1, 'Amoxicillin 500mg', 'antibiotic', 'HSN3004', 150, 20, '2027-12-31', CURRENT_TIMESTAMP);

-- Paracetamol (not low stock)
INSERT INTO products (id, name, category, hsn_code, stock_qty, low_stock_threshold, expiry_date, created_at) VALUES
(2, 'Paracetamol 650mg', 'analgesic', 'HSN3003', 300, 50, '2028-01-01', CURRENT_TIMESTAMP);

-- Ibuprofen (low stock: stock_qty (5) <= threshold (30))
INSERT INTO products (id, name, category, hsn_code, stock_qty, low_stock_threshold, expiry_date, created_at) VALUES
(3, 'Ibuprofen 400mg', 'analgesic', 'HSN3005', 5, 30, '2027-01-01', CURRENT_TIMESTAMP);

-- Azithromycin (low stock: stock_qty (10) <= threshold (15) and expiring soon relative to July 2026)
INSERT INTO products (id, name, category, hsn_code, stock_qty, low_stock_threshold, expiry_date, created_at) VALUES
(4, 'Azithromycin 250mg', 'antibiotic', 'HSN3004', 10, 15, '2026-08-01', CURRENT_TIMESTAMP);

-- Seed Batches
-- Amoxicillin batches
INSERT INTO batches (id, product_id, batch_no, qty, expiry_date, mrp) VALUES
(1, 1, 'AMX001', 100, '2027-12-31', 12.50),
(2, 1, 'AMX002', 50, '2027-06-30', 12.50);

-- Paracetamol batches (Batch 4 is expired relative to current date 2026-07-19)
INSERT INTO batches (id, product_id, batch_no, qty, expiry_date, mrp) VALUES
(3, 2, 'PAR001', 200, '2028-01-01', 2.00),
(4, 2, 'PAR002', 100, '2027-05-01', 2.00);

-- Ibuprofen batch
INSERT INTO batches (id, product_id, batch_no, qty, expiry_date, mrp) VALUES
(5, 3, 'IBU001', 5, '2027-01-01', 5.00);

-- Azithromycin batch
INSERT INTO batches (id, product_id, batch_no, qty, expiry_date, mrp) VALUES
(6, 4, 'AZI001', 10, '2026-08-01', 25.00);


-- Seed Invoices (Status: PENDING)
-- Invoice 1: Standard valid invoice
INSERT INTO invoices (id, customer_id, doctor_id, status, created_at) VALUES
(1, 1, 1, 'PENDING', CURRENT_TIMESTAMP);

INSERT INTO invoice_items (invoice_id, product_id, batch_id, qty, unit_price, gst_rate) VALUES
(1, 1, 1, 5, 12.50, 18.00), -- 5 units from valid Batch AMX001
(1, 2, 3, 10, 2.00, 12.00);  -- 10 units from valid Batch PAR001

-- Invoice 2: Expired Batch Invoice (PAR002 has expired)
INSERT INTO invoices (id, customer_id, doctor_id, status, created_at) VALUES
(2, 2, 2, 'PENDING', CURRENT_TIMESTAMP);

INSERT INTO invoice_items (invoice_id, product_id, batch_id, qty, unit_price, gst_rate) VALUES
(2, 2, 4, 5, 2.00, 12.00);  -- 5 units from expired Batch PAR002

-- Invoice 3: Insufficient Stock Invoice (IBU001 only has 5 qty, but invoice asks for 10)
INSERT INTO invoices (id, customer_id, doctor_id, status, created_at) VALUES
(3, 1, 1, 'PENDING', CURRENT_TIMESTAMP);

INSERT INTO invoice_items (invoice_id, product_id, batch_id, qty, unit_price, gst_rate) VALUES
(3, 3, 5, 10, 5.00, 18.00); -- 10 units from Batch IBU001 (qty available = 5)