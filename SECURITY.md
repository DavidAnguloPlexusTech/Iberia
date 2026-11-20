# Política de seguridad

## Versiones con soporte

| Versión | Soporte |
|---|---|
| 1.0.x | ✅ |
| < 1.0 | ❌ |

## Cómo reportar una vulnerabilidad

No abras un issue público para problemas de seguridad.

Repórtala de forma privada desde la pestaña **Security** del repositorio, en **Report a vulnerability** (GitHub Private Vulnerability Reporting). Incluye, si puedes:

- Descripción del problema y su posible impacto.
- Pasos para reproducirlo y versión afectada.
- Cualquier propuesta de solución.

Recibirás una respuesta lo antes posible. Si se confirma, se publicará una versión con la corrección y un aviso de seguridad (Security Advisory) en este repositorio.

## Medidas activas en el repositorio

- **Dependabot:** alertas de dependencias vulnerables, actualizaciones de seguridad automáticas y actualizaciones de versión semanales de Gradle y GitHub Actions.
- **Code scanning con CodeQL:** default setup de GitHub (sin workflow propio en el repositorio), con análisis de Kotlin y de los workflows en cada push y pull request.
- **Secret scanning** con protección en el push.
