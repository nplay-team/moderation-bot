/**
 * A map of all available permissions and their bit values
 * @example
 * PERMISSIONS_BITMAP.REPORT_READ // => 1
 * PERMISSIONS_BITMAP.REPORT_CREATE // => 10
 */
export const PermissionBitmap = {
	REPORT_READ: 1 << 0,
	REPORT_CREATE: 1 << 1,
	REPORT_DELETE: 1 << 2,
	MODLOG_READ: 1 << 3,
	BAN_APPEAL_MANAGE: 1 << 4,
	PERMISSION_READ: 1 << 5,
	PERMISSION_MANAGE: 1 << 6,
	PARAGRAPH_MANAGE: 1 << 7,
	ADMINISTRATOR: 1 << 8
};

/**
 * An enum that represents all permission keys
 */
export enum PermissionBitmapFlags {
	ReportRead = 'REPORT_READ',
	ReportCreate = 'REPORT_CREATE',
	ReportDelete = 'REPORT_DELETE',
	ModlogRead = 'MODLOG_READ',
	BanAppealManage = 'BAN_APPEAL_MANAGE',
	PermissionRead = 'PERMISSION_READ',
	PermissionManage = 'PERMISSION_MANAGE',
	ParagraphManage = 'PARAGRAPH_MANAGE',
	Administrator = 'ADMINISTRATOR'
}

/**
 * Special permission bitfields
 */
export const PermissionBitmapSpecials = {
	ALL: Object.values(PermissionBitmap).reduce((acc, p) => acc | p, 0),
	NONE: 0
};

/**
 * All available permissions
 */
export type Permission = keyof typeof PermissionBitmap;

/**
 * All available descriptions for permissions
 */
export const PermissionBitmapDescriptions = {
	REPORT_READ: 'Einsehen von Reports',
	REPORT_CREATE: 'Erstellen von Reports',
	REPORT_DELETE: 'Löschen von Reports',
	MODLOG_READ: 'Einsehen des Modlogs',
	BAN_APPEAL_MANAGE: 'Verwalten von Entbannungsanträgen',
	PERMISSION_READ: 'Einsehen von Berechtigungen',
	PERMISSION_MANAGE: 'Vergeben von Berechtigungen',
	PARAGRAPH_MANAGE: 'Verwalten von Regelparagraphen',
	ADMINISTRATOR: 'Administrator'
};

/**
 * Returns the description of a permission
 * @param permission The permission to get the description of
 * @returns The description of the permission
 */
export const getPermissionDescription = (permission: Permission) =>
	PermissionBitmapDescriptions[permission];
