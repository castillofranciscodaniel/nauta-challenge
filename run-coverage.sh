#!/bin/bash

# Ejecutar los tests con cobertura
./gradlew clean test jacocoTestReport

# Verificar si la compilación fue exitosa
if [ $? -eq 0 ]; then
    echo "Tests ejecutados con éxito. Abriendo reporte de cobertura..."

    # Ruta del informe de cobertura
    REPORT_PATH="build/reports/jacoco/test/html/index.html"

    # Verificar si el archivo existe
    if [ ! -f "$REPORT_PATH" ]; then
        echo "Buscando el archivo de informe de cobertura..."
        ALTERNATE_PATH=$(find build/reports -name "index.html" | grep jacoco | head -1)

        if [ -n "$ALTERNATE_PATH" ]; then
            REPORT_PATH="$ALTERNATE_PATH"
            echo "Encontrado informe en: $REPORT_PATH"
        else
            echo "No se encontró el informe de cobertura. Verifique la configuración de Jacoco."
            exit 1
        fi
    fi

    # Determinar qué comando usar para abrir el navegador según el sistema
    if command -v xdg-open &> /dev/null; then
        xdg-open "$REPORT_PATH"
    elif command -v open &> /dev/null; then
        open "$REPORT_PATH"
    else
        echo "Reporte de cobertura generado en: $REPORT_PATH"
        echo "Por favor, abre este archivo en tu navegador para visualizar el reporte."
    fi
else
    echo "Falló la ejecución de los tests. Revisa los errores."
fi
