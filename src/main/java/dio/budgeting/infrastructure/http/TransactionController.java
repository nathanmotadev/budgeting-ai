package dio.budgeting.infrastructure.http;

import dio.budgeting.application.DeleteTransactionUseCase;
import dio.budgeting.application.ListRecentTransactionsUseCase;
import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.SummarizeExpensesUseCase;
import dio.budgeting.application.output.ExpenseSummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints REST "tradicionais". Usam exatamente os mesmos casos de uso que a IA. */
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final PersistTransactionUseCase persistTransaction;
    private final ListTransactionsByCategoryUseCase listByCategory;
    private final ListRecentTransactionsUseCase listRecent;
    private final SummarizeExpensesUseCase summarizeExpenses;
    private final DeleteTransactionUseCase deleteTransaction;

    public TransactionController(PersistTransactionUseCase persistTransaction,
                                 ListTransactionsByCategoryUseCase listByCategory,
                                 ListRecentTransactionsUseCase listRecent,
                                 SummarizeExpensesUseCase summarizeExpenses,
                                 DeleteTransactionUseCase deleteTransaction) {
        this.persistTransaction = persistTransaction;
        this.listByCategory = listByCategory;
        this.listRecent = listRecent;
        this.summarizeExpenses = summarizeExpenses;
        this.deleteTransaction = deleteTransaction;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionRequest request) {
        return TransactionResponse.from(persistTransaction.execute(request.toInput()));
    }

    @GetMapping
    public List<TransactionResponse> listRecent(@RequestParam(defaultValue = "10") int limit) {
        return listRecent.execute(limit).stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/summary")
    public ExpenseSummaryOutput summary(@RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate) {
        return summarizeExpenses.execute(startDate, endDate);
    }

    @GetMapping("/{category}")
    public List<TransactionResponse> listByCategory(@PathVariable Category category) {
        return listByCategory.execute(category).stream().map(TransactionResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        deleteTransaction.execute(id);
    }
}
