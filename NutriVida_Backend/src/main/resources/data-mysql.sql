INSERT INTO servicios (id, nombre, descripcion, tipo, modalidad, precio, duracion_minutos, activo)
VALUES
    ('CN001', 'Primera consulta nutricional', 'Evaluacion inicial: anamnesis, antropometria completa y diseño del primer plan alimenticio.', 'Consulta', 'Presencial', 35000, 50, TRUE),
    ('CN002', 'Control nutricional (seguimiento)', 'Seguimiento mensual: medicion de indicadores y ajuste del plan vigente.', 'Consulta', 'Presencial', 25000, 30, TRUE),
    ('CN003', 'Control nutricional quincenal', 'Seguimiento intensivo cada 15 dias. Recomendado en los primeros 2 meses.', 'Consulta', 'Presencial', 22000, 30, TRUE),
    ('CN004', 'Teleconsulta nutricional', 'Consulta de seguimiento via videollamada. Requiere contar con consulta presencial previa.', 'Consulta', 'Online (video)', 20000, 30, TRUE),
    ('CN005', 'Consulta de urgencia / reagendada', 'Para pacientes que requieren atencion fuera de su control habitual.', 'Consulta', 'Presencial', 28000, 30, TRUE),
    ('PL001', 'Plan perdida de peso (1 mes)', 'Incluye primera consulta + 1 control quincenal + plan alimenticio personalizado + seguimiento por WhatsApp.', 'Plan especializado', 'Presencial', 65000, 50, TRUE),
    ('PL002', 'Plan perdida de peso (3 meses)', 'Incluye primera consulta + 5 controles + 3 planes mensuales + seguimiento continuo.', 'Plan especializado', 'Presencial', 170000, 50, TRUE),
    ('PL003', 'Plan nutricion deportiva (1 mes)', 'Para deportistas y personas con actividad fisica frecuente. Calculo de requerimientos energeticos y proteicos.', 'Plan especializado', 'Presencial', 70000, 50, TRUE),
    ('PL004', 'Plan control diabetes / hipertension', 'Plan adaptado para patologias metabolicas. Coordinacion con medico tratante si aplica.', 'Plan especializado', 'Presencial', 75000, 50, TRUE),
    ('PL005', 'Plan alimentacion vegetariana/vegana', 'Disenado para garantizar aporte adecuado de proteinas, hierro, vitamina B12 y calcio sin productos animales.', 'Plan especializado', 'Presencial', 68000, 50, TRUE),
    ('PL006', 'Plan alimentacion infantil (2-12 años)', 'Evaluacion nutricional pediatrica y diseño de plan adaptado a la etapa de desarrollo del niño.', 'Plan especializado', 'Presencial', 65000, 50, TRUE),
    ('EV001', 'Antropometria completa', 'Peso, talla, IMC, circunferencia de cintura, cadera, brazo y % de grasa corporal con bioimpedanciometria.', 'Evaluacion', 'Presencial', 18000, 20, TRUE),
    ('EV002', 'Bioimpedanciometria', 'Medicion de composicion corporal: masa grasa, masa muscular, agua corporal y edad metabolica.', 'Evaluacion', 'Presencial', 12000, 15, TRUE),
    ('EV003', 'Encuesta de habitos alimentarios', 'Analisis del patron alimentario actual. Identificacion de deficit y excesos nutricionales.', 'Evaluacion', 'Presencial', 10000, 20, TRUE),
    ('EV004', 'Analisis de examenes de laboratorio', 'Interpretacion de hemograma, perfil bioquimico y lipidico en contexto nutricional.', 'Evaluacion', 'Presencial', 15000, 20, TRUE),
    ('TG001', 'Taller de alimentacion saludable', 'Max. 10 personas. Conceptos basicos de alimentacion equilibrada y lectura de etiquetas.', 'Taller grupal', 'Presencial (grupo)', 15000, 90, TRUE),
    ('TG002', 'Taller de cocina nutritiva', 'Preparacion de recetas saludables. Incluye degustacion. Max. 8 personas.', 'Taller grupal', 'Presencial (grupo)', 20000, 120, TRUE),
    ('TG003', 'Taller nutricion para deportistas', 'Hidratacion, nutricion pre y post entrenamiento, suplementacion basica. Max. 12 personas.', 'Taller grupal', 'Presencial (grupo)', 18000, 90, TRUE)
ON DUPLICATE KEY UPDATE id = servicios.id;
