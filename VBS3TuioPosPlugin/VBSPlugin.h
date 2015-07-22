#define VBSPLUGIN_EXPORT __declspec(dllexport)

VBSPLUGIN_EXPORT void WINAPI RegisterCommandFnc(void *executeCommandFnc);
VBSPLUGIN_EXPORT void WINAPI OnSimulationStep(float deltaT);
VBSPLUGIN_EXPORT const char* WINAPI PluginFunction(const char *input);