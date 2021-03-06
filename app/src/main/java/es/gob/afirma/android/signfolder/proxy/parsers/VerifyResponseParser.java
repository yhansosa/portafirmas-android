package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Vector;

import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.signfolder.proxy.RequestVerifyResult;
import es.gob.afirma.android.signfolder.proxy.XmlUtils;

/**
 * Clase encargada de gestionar el parseo de las respuesta de validación.
 */
public class VerifyResponseParser {

    /**
     * Constante que representa el nodo de respuesta principal.
     */
    private static final String VERIFY_RESPONSE_NODE = "verifrp"; //$NON-NLS-1$

    /**
     * Constructor por defecto.
     */
    private VerifyResponseParser() {
        // No instanciable.
    }

    /**
     * Analiza un documento XML y, en caso de tener el formato correcto, obtiene de él
     * un listado de objetos de tipo {@link es.gob.afirma.android.signfolder.proxy.RequestResult}.
     *
     * @param doc Documento XML.
     * @return Objeto con los datos del XML.
     * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
     */
    public static RequestVerifyResult[] parse(final Document doc) {

        if (doc == null) {
            throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
        }

        if (!VERIFY_RESPONSE_NODE.equalsIgnoreCase(doc.getDocumentElement().getNodeName())) {
            throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + //$NON-NLS-1$
                    VERIFY_RESPONSE_NODE + "' y aparece: " + //$NON-NLS-1$
                    doc.getDocumentElement().getNodeName());
        }

        final NodeList requestNodes = doc.getDocumentElement().getChildNodes();
        final Vector<RequestVerifyResult> listRequests = new Vector<>();
        for (int i = 0; i < requestNodes.getLength(); i++) {
            // Nos aseguramos de procesar solo nodos de tipo Element
            i = XmlUtils.nextNodeElementIndex(requestNodes, i);
            if (i == -1) {
                break;
            }
            listRequests.addElement(VerifyResponseParser.VerifyParser.parse(requestNodes.item(i)));
        }

        final RequestVerifyResult[] ret = new RequestVerifyResult[listRequests.size()];
        listRequests.copyInto(ret);
        return ret;
    }

    /**
     * Clase encargada de realizar el parseo de la respuesta del servicio de validación de peticiones.
     */
    private static final class VerifyParser {

        /**
         * Nombre del nodo principal de cada validación.
         */
        private static final String VERIFY_NODE = "verify"; //$NON-NLS-1$

        /**
         * Nombre del atributo asociado al error de la respuesta.
         */
        private static final String ERROR_NODE = "errorMsg"; //$NON-NLS-1$

        /**
         * Nombre del atributo asociado al resultado de la validación.
         */
        private static final String OK_ATTRIBUTE = "ok"; //$NON-NLS-1$

        /**
         * Método encargado de realizar el parseo.
         * @param requestNode Nodo a parsear.
         * @return el resultado de la validación.
         */
        static RequestVerifyResult parse(final Node requestNode) {

            if (!VERIFY_NODE.equalsIgnoreCase(requestNode.getNodeName())) {
                throw new IllegalArgumentException("Se encontro un elemento '" + //$NON-NLS-1$
                        requestNode.getNodeName() + "' en el listado de peticiones"); //$NON-NLS-1$
            }

            boolean ok;

            // Cargamos los atributos
            Node attributeNode;
            final NamedNodeMap attributes = requestNode.getAttributes();

            attributeNode = attributes.getNamedItem(OK_ATTRIBUTE);
            // ok = true, salvo que la propiedad status tenga el valor "KO"
            ok = "true".equalsIgnoreCase(attributeNode.getNodeValue()); //$NON-NLS-1$

            // Recuperamos el mensaje de error en caso de existir.
            Node errorNode = requestNode.getOwnerDocument().getElementsByTagName(ERROR_NODE).item(0);
            String errorMsg = null;
            if(errorNode != null){
                errorMsg = errorNode.getTextContent();
            }

            return new RequestVerifyResult(ok, errorMsg);
        }
    }

}
