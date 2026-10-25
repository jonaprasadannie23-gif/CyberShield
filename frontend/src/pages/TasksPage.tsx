import { FormEvent, useEffect, useState } from "react";
import { api, ApiError } from "../api";
import type { SecurityTask } from "../types";

export function TasksPage() {
  const [tasks, setTasks] = useState<SecurityTask[]>([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    try {
      setTasks(await api.get<SecurityTask[]>("/api/tasks"));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load tasks.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void load(); }, []);

  async function create(e: FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");
    try {
      const created = await api.post<SecurityTask>("/api/tasks", { title, description });
      setTasks((current) => [...current, created]);
      setTitle("");
      setDescription("");
      setSuccess("Task added.");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create task.");
    }
  }

  async function toggle(task: SecurityTask) {
    const updated = await api.put<SecurityTask>(`/api/tasks/${task.taskId}`, {
      title: task.title,
      description: task.description,
      completed: !task.completed,
    });
    setTasks((current) => current.map((item) => item.taskId === updated.taskId ? updated : item));
  }

  async function remove(id: string) {
    await api.delete(`/api/tasks/${id}`);
    setTasks((current) => current.filter((item) => item.taskId !== id));
  }

  const done = tasks.filter((t) => t.completed).length;
  const percent = tasks.length === 0 ? 0 : (done * 100) / tasks.length;

  return (
    <>
      <h1 className="page-title">Safety tasks</h1>
      <p className="page-sub">Track personal follow-up actions. Progress uses your stored tasks.</p>
      {error && <div className="banner error">{error}</div>}
      {success && <div className="banner success">{success}</div>}
      <div className="card" style={{ marginBottom: 16 }}>
        <div className="muted">{done} of {tasks.length} complete</div>
        <div className="progress"><span style={{ width: `${percent}%` }} /></div>
      </div>
      <form className="card" onSubmit={create} style={{ marginBottom: 16 }}>
        <label>Task title</label>
        <input value={title} onChange={(e) => setTitle(e.target.value)} />
        <label>Description</label>
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} />
        <button className="btn btn-primary" style={{ marginTop: 12 }} type="submit">Add task</button>
      </form>
      {loading ? <p className="muted">Loading…</p> : tasks.length === 0 ? (
        <p className="empty">No tasks yet.</p>
      ) : (
        <div className="card">
          {tasks.map((task) => (
            <div key={task.taskId} className="row" style={{ alignItems: "center", marginBottom: 10 }}>
              <label className="checkbox" style={{ flex: 2 }}>
                <input type="checkbox" checked={task.completed} onChange={() => void toggle(task)} />
                <span style={{ textDecoration: task.completed ? "line-through" : "none" }}>{task.title}</span>
              </label>
              <span className="muted" style={{ flex: 2 }}>{task.description}</span>
              <button className="btn btn-danger" onClick={() => void remove(task.taskId)}>Delete</button>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
