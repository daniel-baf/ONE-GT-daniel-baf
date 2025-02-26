package app.Menu.BooksCollection;

import app.Menu.MenuHandler;
import app.Utils.ScreenUtils;
import app.API.Controller.APIController.APIController;
import app.API.Domain.ListBooks;
import app.API.Domain.SearchParameter;
import app.API.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Scanner;


import org.springframework.stereotype.Component;

/**
 * FilterHandler is a concrete implementation of the MenuHandler interface
 * and serves as a menu option handler for adding filters in a chain of
 * responsibility.
 * It allows users to select and specify filters based on available search
 * parameters.
 */
@Component
public class FilterHandler implements MenuHandler {

    private MenuHandler next;
    private final Map<SearchParameter, String> filters;
    private final Scanner scanner;
    private final APIController controller;
    private final BookService bookService;

    @Autowired
    public FilterHandler(Map<SearchParameter, String> filters, Scanner scanner, APIController controller, BookService bookService) {
        this.filters = filters;
        this.scanner = scanner;
        this.controller = controller;
        this.bookService = bookService;
    }

    @Override
    public void handleOption(int option) {
        if (option == 1) {
            aggregateFiltrates();
        } else if (next != null) {
            next.handleOption(option);
        }
    }

    @Override
    public void setNext(MenuHandler next) {
        this.next = next;
    }

    /**
     * Adds filters to a search query based on user selection.
     * <p>
     * This method allows users to select and specify filters interactively
     * from a predefined list of search parameters. Each selected filter requires
     * the user to input a corresponding value. The filters and their values
     * are stored in the `filters` map for later use.
     * <p>
     * Behavior:
     * - Displays the list of available filters, which correspond to the
     * `SearchParameter` enum values.
     * - Prompts the user to select a filter option by entering its number.
     * - Allows entering a filter value after a filter is selected.
     * - Continues until the user inputs "0" to stop adding filters.
     * - Ensures proper user input by validating entries and handling invalid
     * inputs.
     * <p>
     * Workflow:
     * - Enumerates all `SearchParameter` values and displays their names to the
     * user.
     * - Accepts the user's selection of a filter.
     * - Prompts for and stores the value of the selected filter.
     * - Exits when "0" is entered, completing the filter addition process.
     * <p>
     * Notes:
     * - Invalid inputs are handled by prompting the user for a valid input.
     * - Only valid options corresponding to `SearchParameter` indices are accepted.
     * - The `filters` map contains the added filters and their specified values.
     */
    private void aggregateFiltrates() {
        int opcionFiltro = 0;
        while (opcionFiltro != -1) {

            ScreenUtils.cleanScreen("Filtro (0 para continuar): ");
            // Mostrar los filtros disponibles para que el usuario elija
            for (SearchParameter parametro : SearchParameter.values()) {
                System.out.println(parametro.ordinal() + 1 + ". " + parametro.getValue());
            }
            System.out.println("Elige el número del filtro que deseas aplicar (0 para terminar):");

            // Validar entrada
            while (!scanner.hasNextInt()) {
                System.out.println("Por favor, ingresa un número válido.");
                scanner.next(); // Limpiar el buffer de entrada
            }
            opcionFiltro = scanner.nextInt();
            scanner.nextLine(); // Limpiar el buffer de la nueva línea (esto es importante)


            if (opcionFiltro == 0) {
                break; // Si elige 0, termina de agregar filtros
            }

            if (opcionFiltro > 0 && opcionFiltro <= SearchParameter.values().length) {
                SearchParameter filtroSeleccionado = SearchParameter.values()[opcionFiltro - 1];
                System.out.print("Introduce el valor para el filtro " + filtroSeleccionado.getValue() + ": ");
                String valorFiltro = scanner.nextLine(); // Aquí capturamos la entrada del filtro


                // Asegúrate de limpiar el buffer correctamente aquí también, si es necesario
                filters.put(filtroSeleccionado, valorFiltro);
                System.out.println("FILTRO AGREGADO " + filters);
            } else {
                System.out.println("Opción no válida. Elige entre los filtros disponibles o 0 para terminar.");
            }
        }
        ScreenUtils.cleanScreen("... consultando API para filtros " + filters);
        getByFilter();
    }

    private void getByFilter() {
        try {
            ListBooks filteredBooks = this.controller.filterBooks(this.filters, "downloads", 1);
            ScreenUtils.cleanScreen("Se han encontrado " + filteredBooks.getBooks().size() + " libros con los filtros aplicados.\nGuardando en la base de datos");
            // SAVE FILTERED BOOKS
            this.bookService.saveBooksToDatabase(filteredBooks);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}