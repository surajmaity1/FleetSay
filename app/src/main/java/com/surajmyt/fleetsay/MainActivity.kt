package com.surajmyt.fleetsay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.datastore.generated.model.Priority
import com.amplifyframework.datastore.generated.model.Todo
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Manipulating data
        val date = Date()
        val offsetMillis = TimeZone.getDefault().getOffset(date.time).toLong()
        val offsetSeconds = TimeUnit.MILLISECONDS.toSeconds(offsetMillis).toInt()
        val temporalDateTime = Temporal.DateTime(date, offsetSeconds)
        val item = Todo.builder()
            .name("Finish quarterly taxes")
            .priority(Priority.HIGH)
            .completedAt(temporalDateTime)
            .build()

        Amplify.DataStore.save(item,
            { Log.i("Tutorial", "Saved item: ${item.name}") },
            { Log.e("Tutorial", "Could not save item to DataStore", it) }
        )


        // Update a Todo
        Amplify.DataStore.query(Todo::class.java, Where.matches(Todo.NAME.eq("Finish quarterly taxes")),
            { matches ->
                if (matches.hasNext()) {
                    val todo = matches.next()
                    val updatedTodo = todo.copyOfBuilder()
                        .name("File quarterly taxes")
                        .build()
                    Amplify.DataStore.save(updatedTodo,
                        { Log.i("Tutorial", "Updated item: ${updatedTodo.name}") },
                        { Log.e("Tutorial", "Update failed.", it) }
                    )
                }
            },
            { Log.e("Tutorial", "Query failed", it) }
        )
        // Query Todos
        Amplify.DataStore.query(
            Todo::class.java, Where.matches(Todo.PRIORITY.eq(Priority.HIGH)),
            { todos ->
                while (todos.hasNext()) {
                    val todo: Todo = todos.next()
                    Log.i("Tutorial", "==== Todo ====")
                    Log.i("Tutorial", "Name: ${todo.name}")
                    todo.priority?.let { todoPriority -> Log.i("Tutorial", "Priority: $todoPriority") }
                    todo.completedAt?.let { todoCompletedAt -> Log.i("Tutorial", "CompletedAt: $todoCompletedAt") }
                }
            },
            { failure -> Log.e("Tutorial", "Could not query DataStore", failure) }
        )

        // Delete a Todo
        Amplify.DataStore.query(Todo::class.java, Where.matches(Todo.NAME.eq("File quarterly taxes")),
            { matches ->
                if (matches.hasNext()) {
                    val toDeleteTodo = matches.next()
                    Amplify.DataStore.delete(toDeleteTodo,
                        { Log.i("Tutorial", "Deleted item: ${toDeleteTodo.name}") },
                        { Log.e("Tutorial", "Delete failed.", it) }
                    )
                }
            },
            { Log.e("Tutorial", "Query failed.", it) }
        )


    }
}