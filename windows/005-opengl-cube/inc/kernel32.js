const CreateMutexA = (
	lpMutexAttributes/*:number*/,
	bInitialOwner/*:number*/,
	lpName/*:string*/
) => ({
	proc: 'CreateMutexA',
	args: [
		{ value: lpMutexAttributes, size: 'dword', comment: 'LPSECURITY_ATTRIBUTES lpMutexAttributes' },
		{ value: bInitialOwner, size: 'dword', comment: 'BOOL bInitialOwner' },
		{ value: lpName, size: 'dword', comment: 'LPCSTR lpName' },
	],
	ret: { size: 'qword', comment: 'HANDLE' },
});

const GetModuleHandleA = (
	lpModuleName/*:number*/
) => ({
	proc: 'GetModuleHandleA',
	args: [
		{ value: lpModuleName, size: 'dword', comment: 'LPCSTR lpModuleName' }
	],
	ret: { size: 'qword', comment: 'HMODULE *phModule' },
});

module.exports = {
	CreateMutexA,
	GetModuleHandleA,
};