/*
  Warnings:

  - You are about to drop the `reports` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropForeignKey
ALTER TABLE `reports` DROP FOREIGN KEY `reports_issuerId_fkey`;

-- DropForeignKey
ALTER TABLE `reports` DROP FOREIGN KEY `reports_paragraphId_fkey`;

-- DropForeignKey
ALTER TABLE `reports` DROP FOREIGN KEY `reports_userId_fkey`;

-- DropTable
DROP TABLE `reports`;

-- CreateTable
CREATE TABLE `moderations` (
    `id` VARCHAR(191) NOT NULL,
    `number` INTEGER NOT NULL,
    `userId` VARCHAR(191) NOT NULL,
    `guildId` VARCHAR(191) NOT NULL,
    `action` ENUM('WARN', 'TIMEOUT', 'KICK', 'TEMP_BAN', 'BAN') NOT NULL,
    `status` ENUM('OPENED', 'EXECUTED', 'DONE', 'REVERTED') NOT NULL DEFAULT 'OPENED',
    `reason` VARCHAR(191) NULL,
    `paragraphId` VARCHAR(191) NULL,
    `message` LONGTEXT NULL,
    `duration` DATETIME(3) NULL,
    `delDays` INTEGER NULL,
    `issuerId` VARCHAR(191) NOT NULL,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedAt` DATETIME(3) NOT NULL,

    INDEX `moderations_userId_guildId_number_idx`(`userId`, `guildId`, `number`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `moderations` ADD CONSTRAINT `moderations_userId_fkey` FOREIGN KEY (`userId`) REFERENCES `users`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `moderations` ADD CONSTRAINT `moderations_paragraphId_fkey` FOREIGN KEY (`paragraphId`) REFERENCES `paragraphs`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `moderations` ADD CONSTRAINT `moderations_issuerId_fkey` FOREIGN KEY (`issuerId`) REFERENCES `users`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
