import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import App from "./App.jsx";

describe("App", () => {
  it("renders the headline", () => {
    render(<App />);
    expect(screen.getByText(/Bring clarity/i)).toBeInTheDocument();
  });
});
