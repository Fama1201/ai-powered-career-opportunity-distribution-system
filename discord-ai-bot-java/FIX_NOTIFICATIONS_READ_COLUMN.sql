-- Fix notifications table: rename "read" column to "is_read" and ensure "type" column existsnu
-- PostgreSQL'de "read" reserved keyword olduğu için is_read kullanıyoruz

-- Step 1: Add type column if it doesn't exist (optional, for future use)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'notifications' 
        AND column_name = 'type'
    ) THEN
        ALTER TABLE notifications ADD COLUMN type VARCHAR(50);
    END IF;
END $$;

-- Step 2: Check if old "read" column exists and add new column if needed
DO $$
BEGIN
    -- Add new column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'notifications' 
        AND column_name = 'is_read'
    ) THEN
        ALTER TABLE notifications ADD COLUMN is_read BOOLEAN DEFAULT false;
    END IF;
    
    -- Copy data from old column to new column (if old column exists)
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'notifications' 
        AND column_name = 'read'
    ) THEN
        UPDATE notifications SET is_read = "read" WHERE is_read IS NULL OR is_read = false;
    END IF;
END $$;

-- Step 3: Set default for existing rows
UPDATE notifications SET is_read = false WHERE is_read IS NULL;

-- Step 4: Make column NOT NULL
ALTER TABLE notifications ALTER COLUMN is_read SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN is_read SET DEFAULT false;

-- Step 5: Drop old column if it exists (be careful with this!)
-- Uncomment the line below only after verifying the migration worked correctly
-- ALTER TABLE notifications DROP COLUMN IF EXISTS "read";

