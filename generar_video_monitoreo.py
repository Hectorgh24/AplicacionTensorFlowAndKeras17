import json
import os
import matplotlib.pyplot as plt
import matplotlib.animation as animation

# Lista de clases en el mismo orden que la aplicación Android
CLASS_LIST = [
    "De pie",
    "Levantándose",
    "Caminando",
    "Corriendo",
    "Subiendo",
    "Saltando",
    "Bajando",
    "Acostado",
    "Sentándose",
    "Caída frontal",
    "Caída a la derecha",
    "Caída hacia atrás",
    "Caída contra obstáculo",
    "Caída (protección)",
    "Caída al sentarse",
    "Desmayo / Síncope",
    "Caída a la izquierda"
]

def cargar_datos(json_path):
    if not os.path.exists(json_path):
        print(f"Error: No se encontró el archivo '{json_path}'.")
        print("Asegúrate de copiar el JSON exportado de la aplicación en esta misma carpeta.")
        return None
    with open(json_path, 'r', encoding='utf-8') as f:
        return json.load(f)

def generar_video(json_path="datos-monitoreo-tensorflow-keras-17-clases.json", output_path="linea_tiempo_monitoreo.mp4"):
    data = cargar_datos(json_path)
    if not data:
        return

    history = data.get("predictionHistory", [])
    if not history:
        print("El historial de predicciones está vacío. No hay datos para graficar.")
        return

    duration = data.get("durationSeconds", 30)
    print(f"Generando animación de la línea de tiempo. Duración: {duration}s. Predicciones registradas: {len(history)}")

    # Configuración de la figura de Matplotlib
    fig, ax = plt.subplots(figsize=(10, 6), dpi=100)
    
    # Mapeo de posiciones para el eje Y
    y_positions = list(range(len(CLASS_LIST)))
    
    # Límites iniciales de visualización (ventana deslizante de 60 segundos)
    max_window = 60  # Ventana de tiempo visible igual a la aplicación
    
    # Configurar estilos estéticos (estilo oscuro premium)
    fig.patch.set_facecolor('#1E1E1E')
    ax.set_facecolor('#121212')
    
    ax.set_yticks(y_positions)
    ax.set_yticklabels(CLASS_LIST, color='#E0E0E0', fontsize=8)
    ax.tick_params(colors='#E0E0E0')
    
    # Título y etiquetas
    ax.set_title("Línea de Tiempo de Actividades y Caídas (Reconstrucción)", color='#FFFFFF', fontsize=12, fontweight='bold', pad=15)
    ax.set_xlabel("Tiempo (segundos)", color='#E0E0E0', fontsize=10, labelpad=10)
    ax.grid(True, which='both', color='#2C2C2C', linestyle='--', linewidth=0.5)

    # Listas de puntos a graficar
    x_normal, y_normal = [], []
    x_fall, y_fall = [], []

    # Inicializar scatter plots en el gráfico
    scatter_normal = ax.scatter([], [], color='#00E5FF', s=50, label='Actividades normales', edgecolors='none')
    scatter_fall = ax.scatter([], [], color='#FF1744', s=60, label='Caídas detectadas', edgecolors='none')
    ax.legend(loc='upper right', facecolor='#1E1E1E', edgecolor='#2C2C2C', labelcolor='#E0E0E0')

    # Línea vertical del tiempo actual
    time_line = ax.axvline(x=0, color='#FFC107', linestyle='-', alpha=0.8, linewidth=1.5)

    # Ajustes de bordes estéticos
    for spine in ax.spines.values():
        spine.set_color('#2C2C2C')

    def init():
        scatter_normal.set_offsets(list(zip([], [])))
        scatter_fall.set_offsets(list(zip([], [])))
        time_line.set_xdata([0, 0])
        ax.set_xlim(0, max_window)
        return scatter_normal, scatter_fall, time_line

    def update(frame):
        # frame representa el segundo actual de la simulación (de 0 a duration)
        
        # Filtrar predicciones que ocurrieron hasta el segundo 'frame'
        current_predictions = [p for p in history if p['timeSeconds'] <= frame]
        
        norm_points = []
        fall_points = []
        
        for p in current_predictions:
            t = p['timeSeconds']
            class_name = p['className']
            if class_name in CLASS_LIST:
                y_idx = CLASS_LIST.index(class_name)
                
                # Distinguir entre caídas (índices 9 a 16) y actividades normales (0 a 8)
                if y_idx >= 9:
                    fall_points.append((t, y_idx))
                else:
                    norm_points.append((t, y_idx))

        # Actualizar datos de los scatters
        if norm_points:
            scatter_normal.set_offsets(norm_points)
        if fall_points:
            scatter_fall.set_offsets(fall_points)

        # Actualizar línea de tiempo actual
        time_line.set_xdata([frame, frame])

        # Simular comportamiento de la ventana deslizante (scrolling)
        if frame > max_window:
            ax.set_xlim(frame - max_window, frame)
        else:
            ax.set_xlim(0, max_window)

        return scatter_normal, scatter_fall, time_line

    # Crear la animación (1 frame por segundo)
    # duration + 2 segundos para dar holgura al final
    frames_total = int(duration) + 2
    ani = animation.FuncAnimation(
        fig, update, frames=frames_total, init_func=init, blit=False, interval=1000
    )

    # Intentar guardar el archivo de video
    # Nota: requiere tener instalado 'ffmpeg' en el sistema del usuario
    try:
        print("Guardando video como MP4 (requiere ffmpeg instalado en el sistema)...")
        ani.save(output_path, writer='ffmpeg', fps=1)
        print(f"¡Éxito! Video guardado en '{output_path}'.")
    except Exception as e:
        print(f"\nNo se pudo compilar a MP4 automáticamente debido a: {e}")
        print("Alternativa: Guardando como GIF animado...")
        try:
            output_gif = output_path.replace(".mp4", ".gif")
            ani.save(output_gif, writer='pillow', fps=1)
            print(f"¡Éxito! Archivo guardado como GIF en '{output_gif}'.")
        except Exception as gif_error:
            print(f"No se pudo guardar como GIF: {gif_error}")
            print("\nConsejo: Para compilar a MP4 en Windows, instala ffmpeg e incorpóralo a tus variables de entorno.")

if __name__ == "__main__":
    # Nombre del archivo JSON por defecto que exporta la app
    generar_video()
