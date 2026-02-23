export default function App() {
  return (
    <main className="page">
      <header className="hero">
        <p className="eyebrow">Group Management</p>
        <h1>Bring clarity to every team</h1>
        <p className="lead">
          Track groups, members, and responsibilities in one place.
        </p>
        <div className="actions">
          <button className="primary">Create group</button>
          <button className="ghost">View dashboard</button>
        </div>
      </header>
      <section className="grid">
        <article>
          <h2>Unified roster</h2>
          <p>Invite, tag, and assign members with a single flow.</p>
        </article>
        <article>
          <h2>Momentum tracking</h2>
          <p>See active groups and pending approvals in seconds.</p>
        </article>
        <article>
          <h2>Smart notifications</h2>
          <p>Keep everyone aligned with built-in reminders.</p>
        </article>
      </section>
    </main>
  );
}
