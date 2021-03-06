package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestDetail;
import es.gob.afirma.android.util.PfLog;

/**
 * Tarea para la carga de los detalles de una petici&oacute;n en una pantalla para la
 * visualizaci&oacute;n de la descripci&oacute;n de peticiones.
 */
public final class LoadPetitionDetailsTask extends AsyncTask<Void, Void, RequestDetail> {

    /**
     * Codigo de error de autenticacion (perdida de sesion)
     */
    private static final String AUTH_ERROR = "ERR-11"; //$NON-NLS-1$
    private final String petitionId;
    private final CommManager commManager;
    private final LoadSignRequestDetailsListener listener;
    private final String ownerId;
    private boolean lostSession = false;

    /**
     * Crea la tarea para la carga de los detalles de una petici&oacute;n en una pantalla para la
     * visualizaci&oacute;n de la descripci&oacute;n de peticiones.
     *
     * @param petitionId  Identificados de la petici&oacute;n de la que se quiere el detalle.
     * @param ownerId     DNI del propietario de la petición.
     * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
     * @param listener    Actividad en la que es posible mostrar los datos.
     */
    public LoadPetitionDetailsTask(final String petitionId, final String ownerId,
                                   final CommManager commManager, final LoadSignRequestDetailsListener listener) {
        this.petitionId = petitionId;
        this.ownerId = ownerId;
        this.commManager = commManager;
        this.listener = listener;
    }

    @Override
    protected RequestDetail doInBackground(final Void... args) {

        RequestDetail requestDetail;
        try {
            requestDetail = this.commManager.getRequestDetail(this.petitionId, this.ownerId);
        } catch (final Exception e) {
            requestDetail = null;
            PfLog.e(SFConstants.LOG_TAG, "Ocurrio un error al recuperar las peticiones de firma: " + e); //$NON-NLS-1$
            // Si se ha perdido la sesion vuelve a la pantalla de login
            if (e.getMessage().contains(AUTH_ERROR)) {
                lostSession = true;
                this.listener.lostSession();
            }
        } catch (final Throwable e) {
            PfLog.w(SFConstants.LOG_TAG, "No se pudo obtener el detalle de la solicitud: " + e); //$NON-NLS-1$
            requestDetail = null;
        }

        return requestDetail;
    }

    @Override
    protected void onPostExecute(final RequestDetail result) {
        if (result != null) {
            this.listener.loadedSignRequestDetails(result);
        } else if (!lostSession) {
            this.listener.errorLoadingSignRequestDetails();
        }
    }

    /**
     * Interfaz con los metodos para gestionar los resultados de la peticion del detalle
     * de una solicitud de firma.
     */
    public interface LoadSignRequestDetailsListener {
        void loadedSignRequestDetails(RequestDetail details);

        void errorLoadingSignRequestDetails();

        void lostSession();
    }
}
